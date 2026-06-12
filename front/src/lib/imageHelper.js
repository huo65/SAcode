import axios from "axios";
import { Storage } from "@/api/apis";

const getRawFile = (file) => file?.raw || file;
const IMAGE_TYPES = ["image/jpeg", "image/png", "image/webp", "image/gif"];
const MAX_IMAGE_SIZE = 2 * 1024 * 1024;

const buildFormData = (files, category) => {
  const formData = new FormData();
  files.forEach((file) => {
    formData.append("files", file);
  });
  formData.append("category", category);
  return formData;
};

const requestUpload = async (files, category) => {
  if (!files.length) {
    return [];
  }

  const token = sessionStorage.getItem("token");
  const response = await axios.post(
    Storage.upload.url,
    buildFormData(files, category),
    {
      headers: token ? { Token: token } : {},
    }
  );

  if (!response?.data || response.data.code !== 1) {
    throw new Error(response?.data?.msg || "图片上传失败");
  }

  return response.data.data?.urls || [];
};

const requestSingleUpload = async (file, category, onProgress) => {
  const token = sessionStorage.getItem("token");
  const response = await axios.post(
    Storage.upload.url,
    buildFormData([file], category),
    {
      headers: token ? { Token: token } : {},
      onUploadProgress: (event) => {
        if (!event.total || !onProgress) return;
        const percent = Math.min(
          100,
          Math.round((event.loaded / event.total) * 100)
        );
        onProgress(percent, event);
      },
    }
  );

  if (!response?.data || response.data.code !== 1) {
    throw new Error(response?.data?.msg || "图片上传失败");
  }

  return response.data.data?.urls?.[0] || "";
};

export const uploadImageListFromRawFiles = async (
  fileList,
  category = "product"
) => {
  const rawFiles = (fileList || [])
    .map((item) => getRawFile(item))
    .filter(Boolean);
  return requestUpload(rawFiles, category);
};

export const uploadImageFromRawFile = async (
  file,
  category = "avatar"
) => {
  const rawFile = getRawFile(file);
  if (!rawFile) {
    return "";
  }
  const urls = await requestUpload([rawFile], category);
  return urls[0] || "";
};

export const uploadImageFromRawFileWithProgress = async (
  file,
  category = "avatar",
  onProgress
) => {
  const rawFile = getRawFile(file);
  if (!rawFile) {
    return "";
  }
  return requestSingleUpload(rawFile, category, onProgress);
};

export const validateImageFile = (file) => {
  const rawFile = getRawFile(file);
  if (!rawFile) {
    return { valid: false, message: "未选择图片文件" };
  }
  if (!IMAGE_TYPES.includes(rawFile.type)) {
    return { valid: false, message: "仅支持 JPG、PNG、WEBP、GIF 图片" };
  }
  if (rawFile.size > MAX_IMAGE_SIZE) {
    return { valid: false, message: "图片大小不能超过 2MB" };
  }
  return { valid: true, message: "" };
};

export const getFileNameFromUrl = (url = "") => {
  const normalizedUrl = url.split("?")[0];
  const segments = normalizedUrl.split("/");
  return segments[segments.length - 1] || "uploaded-file";
};
