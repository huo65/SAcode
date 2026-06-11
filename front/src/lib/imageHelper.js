const preStr = "/img/"; // 上传时只上传 /img 目录下的图片(开发时放在 public/img 下, 打包会打在 dist/img 里)

export const generateImageListFromRawFiles = (fileList) => {
  let imageList = [];
  imageList = fileList?.map((item) => preStr + item.name);
  return imageList;
};

export const generateImageFromRawFile = (file) => {
  return preStr + file.name;
};
