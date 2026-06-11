import axios from "axios";
// import qs from "qs";
import { ElMessage as message } from "element-plus";

const instance = axios.create({
  timeout: 20000,
  validateStatus(status) {
    return status >= 200 && status < 300;
  },
});

// 获取 jwt
instance.interceptors.request.use(
  (config) => {
    const jwt = sessionStorage.getItem("token");
    if (jwt) {
      // config.headers.Authorization = `Bearer ${jwt}`;
      config.headers.Token = `${jwt}`;
    }
    return config;
  },
  (err) => {
    return Promise.reject(err);
  }
);

// 清除掉对象中的空值
const shakeData = (data) => {
  const _data = { ...data };
  const nils = [undefined, null, ""];
  for (let key in _data) {
    let val = _data[key];
    if (typeof val === "object" && val !== null) {
      if (!Array.isArray(val) && !Object.keys(val).length) delete _data[key];
      // else _data[key] = shakeData(val);
    } else {
      if (nils.includes(val)) {
        delete _data[key];
      }
    }
  }

  // console.log("shake 后的data", _data);
  return _data;
};

const fetch = (info, data) => {
  const { method, url, inQuery } = info;

  if (data) {
    // console.log("shake之前的data", data);
    data = shakeData(data);
  }

  const headers =
    method !== "get"
      ? {
          "Content-Type": "application/json",
        }
      : undefined;

  // console.log(
  //   "%c🦅🦅DEBUG🦅🦅 method, url, headers, data, inQuery",
  //   "font-size:16px;font-weight:700",
  //   method,
  //   url,
  //   headers,
  //   data,
  //   inQuery
  // );

  const requestConfig =
    method === "get" || method === "delete"
      ? {
          method,
          url,
          params: data,
        }
      : {
          method,
          url,
          data: !inQuery ? data : undefined,
          params: inQuery ? data : undefined,
          headers,
        };

  return instance(requestConfig)
    .catch((err) => {
      // 处理请求失败
      // console.log("第一层catch的err", err);
      message.error("request failed");
      return Promise.reject(err);
    })
    .then((res) => {
      // console.log("第二层then中的res", res)
      if (!res.data) {
        message.error("the response is empty");
        return Promise.reject();
      }
      return Promise.resolve(res.data);
    })
    .then((res) => {
      // 请求成功，检查业务（code）是否成功
      console.log("check code", res);
      if (!/mock/.test(url) && res.code !== 1) {
        message.error(res.msg ?? "res.code error");
        return Promise.reject(res.data);
      } else {
        return Promise.resolve(res.data);
      }
    });
};

export default fetch;
