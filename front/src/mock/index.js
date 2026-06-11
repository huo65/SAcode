import { cloneDeep } from "lodash";

// 商品类别
const goodsCategories = [
  {
    label: "category1",
    value: 1,
  },
  {
    label: "category2",
    value: 2,
  },
  {
    label: "category3",
    value: 3,
  },
];

// 商品详情信息
const prod_detail = {
  id: "id",
  name: "name",
  description: "descriptio",
  price: 0,
  mer: "mer_id",
  cat_name: "category_name",
  number: 0,
  prod_image_list: [
    {
      prod_id: "prod_id",
      image: "iamgeString",
    },
  ],
};

const prePath = "/img";
const imageUrlMap = {
  iamgeString: `${prePath}/coca.jpg`,
  coca: `${prePath}/coca.jpg`,
  coca2: `${prePath}/coca2.jpg`,
  coca3: `${prePath}/coca3.jpg`,
};

// 商品列表
const prod_list = [];
for (let i = 0; i < 100; i++) {
  let curProd = cloneDeep(prod_detail);
  const hackProps = {
    price: i,
    id: String(i),
    name: "name_" + i,
    cat_name: "category" + ((i % 3) + 1),
  };
  curProd = { ...curProd, ...hackProps };
  prod_list.push(curProd);
}

prod_list[0] = {
  ...prod_list[0],
  name: "long Name long Name long Name long Name long Name  ",
  description:
    "This is the product description. This is the product description. This is the product description. This is the product description. This is the product descriptption. This is the product description. This is the product description. This is the product description. This is the product description. This is the product description. This is the product description.ion. This is the product description. This is the product description. This is the product description. This is the product description. This is the product description. This is the product description. This is the product description. ",
  prod_image_list: [
    {
      prod_id: "prod_id",
      image: "coca3",
    },
    {
      prod_id: "prod_id",
      image: "coca2",
    },
    {
      prod_id: "prod_id",
      image: "coca3",
    },
  ],
};

export { goodsCategories, imageUrlMap, prod_list };
