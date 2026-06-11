//const host = "https://mock.apifox.com/m1/4254846-0-default";
const host = "//127.0.0.1:8080";
// const host = "//192.168.111.56:8080";

// 用户相关接口信息
export const User = {
    // 用户注册
    register: {
        method: "post",
        url: `${host}/user/register`,
    },

    // 用户登录
    login: {
        method: "post",
        url: `${host}/user/login`,
    },

    // 获取用户信息
    getInfo: {
        method: "get",
        url: `${host}/user/getinfo`,
    },

    // 更新用户信息
    updateInfo: {
        method: "post",
        url: `${host}/user/update`,
    },

    // 添加地址
    addAddress: {
        method: "post",
        url: `${host}/user/addAdress`,
    },

    // 获取用户的地址
    getAddress: {
        method: "get",
        url: `${host}/user/address`,
    },

    // 删除用户的地址
    deleteAddress: {
        method: "delete",
        url: `${host}/user/deleteAddress`,
    },
};

// 商品相关接口信息
export const Product = {
    // 获取已有的所有分类
    getCategories: {
        method: "get",
        url: `${host}/category`,
    },

    // 商家上传商品（等待管理员审核）
    addProduct: {
        method: "post",
        url: `${host}/product/add`,
    },

    // 删除商品
    deleteProduct: {
        method: "delete",
        url: `${host}/product/delete`,
    },

    // 获取商品列表
    getProductList: {
        method: "get",
        url: `${host}/product/search`,
    },

    // 获取商品的详情信息
    getProductInfo: {
        method: "get",
        url: `${host}/product/info`,
    },

    // 编辑商品信息
    editProductInfo: {
        method: "post",
        url: `${host}/product/update`,
    },

    // 添加商品类别
    addCategory: {
        method: "post",
        url: `${host}/category/add`,
    },

    // 删除商品类别
    deleteCategory: {
        method: "delete",
        url: `${host}/category/delete`,
    },

    // 管理员审核商品(变更商品状态)
    checkProduct: {
        method: "get",
        url: `${host}/product/check`,
    },
};

// 订单相关接口信息
export const Order = {
    // 创建订单(单个)
    createOrder: {
        method: "post",
        url: `${host}/user/order`,
    },

    // 支付订单
    payOrder: {
        method: "post",
        url: `${host}/user/pay`,
        inQuery: true,
    },

    // 支付订单2 Temp: 这个没有inQuery
    payOrder2: {
        method: "post",
        url: `${host}/user/pay`,
    },

    // 获取订单列表
    getOrderList: {
        method: "post",
        url: `${host}/order/get`,
        inQuery: true,
    },

    // 变更订单状态
    updateOrder: {
        method: "post",
        url: `${host}/order/update`,
    },

    // 筛选订单
    filterOrder: {
        method: "get",
        url: `${host}/order/filter`,
    },
};

export const Cart = {
    // 获取购物车信息
    getCart: {
        method: "get",
        url: `${host}/cart/getCart`,
    },

    // 变更购物车信息
    updateCart: {
        method: "post",
        url: `${host}/cart/update`,
    },

    // 批量下单
    submitOrderList: {
        method: "post",
        url: `${host}/cart/makeOrder`,
    },
};