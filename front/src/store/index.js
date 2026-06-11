import { createStore } from "vuex";
import { computed } from "vue";

const store = createStore({
    state() {
        return {
            isTEST: false,

            // 用户身份信息
            userInfo: JSON.parse(sessionStorage.getItem("userInfo") || '{"status":"guest"}'),
            category_list: [], // 分类信息
            cart_list: [], // 本地存储的购物车信息
            refreshDataFnMap: {
                // 存放tab切换时要触发的函数，用于自动刷新数据
                Category: null,
                Goods: null,
                Order: null,
                Info: null,
            },
        };
    },
    actions: {
        changeStatus({ commit }, status) {
            commit("changeStatus", status);
        },
    },
    mutations: {
        changeStatus(state, status) {
            state.userInfo.status = status || "guest";
        },

        setUserInfo(state, info) {
            const map = {
                cus: "customer",
                mer: "merchant",
                admin: "admin",
                driver: "driver"
            };
            info.status = map[info.type] || "guest";
            state.userInfo = {
                ...state.userInfo,
                ...info,
            };
            sessionStorage.setItem("userInfo", JSON.stringify(state.userInfo));
            //console.log("state.userInfo = ", state.userInfo);
        },
        clearUserInfo(state) {
            state.userInfo = { status: "guest" };
            sessionStorage.removeItem("userInfo");
            sessionStorage.removeItem("token");
            sessionStorage.removeItem("guestMode");
        },
        setCategoryList(state, list) {
            state.category_list = list;
        },
        updateCartList(state, list) {
            state.cart_list = list;
        },
        updataRefreshDataFnMap(state, { tabLabel, fn }) {
            state.refreshDataFnMap[tabLabel] = fn;
        },
    },
});

export default store;
export const curStatus = computed(() => store.state.userInfo.status);
export const userInfo = computed(() => store.state.userInfo);
export const isTEST = computed(() => store.state.isTEST);
export const productCategories = computed(() => store.state.category_list);
export const cartList = computed(() => store.state.cart_list);
export const refreshDataFnMap = computed(() => store.state.refreshDataFnMap);
