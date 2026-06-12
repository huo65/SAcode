import axios from "axios";
import { Storage } from "@/api/apis";

const getRawFile = (file) => file?.raw || file;

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
    throw new Error(response?.data?.msg || "Upload failed");
  }

  return response.data.data?.urls || [];
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

export const getFileNameFromUrl = (url = "") => {
  const normalizedUrl = url.split("?")[0];
  const segments = normalizedUrl.split("/");
  return segments[segments.length - 1] || "uploaded-file";
};
