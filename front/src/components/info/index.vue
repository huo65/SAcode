<script setup>
import { computed, ref, reactive, onMounted } from "vue";
import $store, { userInfo } from "@/store";
import fetch from "@/api/fetch";
import { User } from "@/api/apis";
import { ElMessage } from "element-plus";
import { STATUS_MAP } from "@/constant";
import { uploadImageFromRawFile } from "@/lib/imageHelper.js";
const modifyFormVisible = ref(false);
const modifyAddressVisible = ref(false);
const formLabelWidth = "140px";
const modifyAddressData = ref("");
// 定义状态变量
const previewImageUrl = ref(null);
const fileInput = ref(null);

// 选择文件的方法
const chooseFile = () => {
  fileInput.value.click();
};

// 文件上传及预览处理
const uploadFile = async (event) => {
  const file = event.target.files[0];
  if (file) {
    const reader = new FileReader();
    reader.onload = (e) => {
      previewImageUrl.value = e.target.result;
    };
    reader.readAsDataURL(file);
    try {
      modifyData.portrait = await uploadImageFromRawFile(file, "avatar");
      modifyUserPortrait();
    } catch (error) {
      previewImageUrl.value = null;
      ElMessage.error(error?.message || "Upload avatar failed");
    } finally {
      event.target.value = "";
    }
  }
};

const modifyData = reactive({
  id: "",
  type: "",
  name: "",
  portrait: "",
  password: "",
  phone: "",
  description: "",
  ...userInfo.value, // 初始化赋值为login获取到的用户信息
});
const isDriverUser = computed(() => userInfo.value?.type === "driver");
const driverProfileFields = computed(() => ({
  driverIdCard: modifyData.driverIdCard || "",
  driverVehicle: modifyData.driverVehicle || "",
  driverEmergencyContact: modifyData.driverEmergencyContact || "",
  driverServiceArea: modifyData.driverServiceArea || userInfo.value.driverServiceArea || "",
}));

const addressData = ref([]);
const currentUserType = computed(() => {
  return (
    STATUS_MAP[userInfo.value?.type] || {
      label: "未知身份",
      value: userInfo.value?.type || "-",
    }
  );
});

const fetchAddressData = async () => {
  fetch(User.getAddress, { id: userInfo.value.id }).then((data) => {
    addressData.value = data;
    $store.commit("setUserInfo", { ...userInfo.value, addr_list: data }); // 把地址添加到其中
  });
};

const handleDelete = (row) => {
  fetch(User.deleteAddress, {
    addressId: row.addrId,
  })
    .then(() => {
      ElMessage.success("Delete Address Successfully.");
      fetchAddressData();
    })
    .finally(() => {
      modifyAddressVisible.value = false;
    });
  // ElMessage.error("未实现");
};
const handleAdd = () => {
  if (!modifyAddressData.value) return;
  fetch(User.addAddress, {
    usr: userInfo.value.id,
    location: modifyAddressData.value,
  })
    .then(() => {
      ElMessage.success("Add Address Successfully.");
      fetchAddressData();
    })
    .finally(() => {
      modifyAddressVisible.value = false;
    });
};

const modifyUserInfo = () => {
  const payload = {
    id: modifyData.id,
    type: modifyData.type,
    name: modifyData.name,
    portrait: modifyData.portrait,
    password: modifyData.password,
    phone: modifyData.phone,
    description: modifyData.description,
  };
  fetch(User.updateInfo, payload)
    .then((data) => {
      ElMessage.success("Update user info successfully");
      fetch(User.getInfo, { id: userInfo.value.id }).then((data) => {
        console.log("@@@getUserInfo", data);
        const newInfo = {
          ...userInfo.value,
          ...data,
          type: userInfo.value.type, // mock的时候才不会因为随机而改变当前的身份
        };
        $store.commit("setUserInfo", newInfo);
        if (isDriverUser.value) {
          $store.commit("patchUserInfo", driverProfileFields.value);
        }
      });
    })
    .finally(() => {
      modifyFormVisible.value = false;
    });
};

const modifyUserPortrait = () => {
  fetch(User.updateInfo, {
    id: modifyData.id,
    portrait: modifyData.portrait,
  }).then((data) => {
    ElMessage.success("Update user portrait successfully");
    fetch(User.getInfo, { id: userInfo.value.id }).then((data) => {
      console.log("@@@getUserInfo", data);
      const newInfo = {
        ...userInfo.value,
        ...data,
        type: userInfo.value.type, // mock的时候才不会因为随机而改变当前的身份
      };
      $store.commit("setUserInfo", newInfo);
    });
  });
};

const initInfoData = () => {
  fetchAddressData();
};

onMounted(() => {
  $store.commit("updataRefreshDataFnMap", {
    tabLabel: "Info",
    fn: initInfoData,
  });
  initInfoData();
});
</script>

<template>
  <!-- 修改数据表单 -->
  <!-- {{ userInfo.id }} -->
  <el-dialog v-model="modifyFormVisible" title="Modify Information" width="500">
    <el-form :model="modifyData">
      <el-form-item label="UserName" :label-width="formLabelWidth">
        <el-input v-model="modifyData.name" autocomplete="off" />
      </el-form-item>
      <el-form-item label="Description" :label-width="formLabelWidth">
        <el-input v-model="modifyData.description" autocomplete="off" />
      </el-form-item>
      <el-form-item label="PhoneNumber" :label-width="formLabelWidth">
        <el-input v-model="modifyData.phone" autocomplete="off" />
      </el-form-item>
        <template v-if="isDriverUser">
          <el-form-item label="ID Card" :label-width="formLabelWidth">
            <el-input
              v-model="modifyData.driverIdCard"
              autocomplete="off"
              placeholder="课堂展示版补录"
            />
          </el-form-item>
          <el-form-item label="Vehicle" :label-width="formLabelWidth">
            <el-input
              v-model="modifyData.driverVehicle"
              autocomplete="off"
              placeholder="电动车 / 摩托车 / 自行车"
            />
          </el-form-item>
          <el-form-item label="Service Area" :label-width="formLabelWidth">
            <el-input
              v-model="modifyData.driverServiceArea"
              autocomplete="off"
              placeholder="例如：大学城 / 科技园"
            />
          </el-form-item>
          <el-form-item label="Emergency Contact" :label-width="formLabelWidth">
            <el-input
              v-model="modifyData.driverEmergencyContact"
              autocomplete="off"
              placeholder="紧急联系人电话"
            />
          </el-form-item>
        </template>
      <el-form-item label="Password" :label-width="formLabelWidth">
        <el-input
          v-model="modifyData.password"
          type="password"
          show-password
          autocomplete="off"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="modifyFormVisible = false">Cancel</el-button>
        <el-button type="primary" @click="modifyUserInfo"> Confirm </el-button>
      </div>
    </template>
  </el-dialog>

  <!-- 添加地址表单 -->
  <el-dialog
    v-model="modifyAddressVisible"
    title="Add Address"
    width="500"
    @close="(modifyAddressVisible = false), (modifyAddressData = '')"
  >
    <el-form>
      <el-form-item label="New Address" :label-width="formLabelWidth">
        <el-input v-model="modifyAddressData" autocomplete="off" />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button
          @click="(modifyAddressVisible = false), (modifyAddressData = '')"
          >Cancel</el-button
        >
        <el-button type="primary" @click="handleAdd()"> Confirm </el-button>
      </div>
    </template>
  </el-dialog>

  <el-container>
    <!-- 头像 -->
    <el-aside width="200px">
      <img
        v-if="previewImageUrl"
        :src="previewImageUrl"
        @click="chooseFile"
        alt="预览"
        class="preview-image"
      />
      <img
        :src="userInfo.portrait || '/default_avatar.jpg'"
        v-else
        @click="chooseFile"
        class="preview-image"
      />
      <input
        type="file"
        ref="fileInput"
        accept="image/*"
        @change="uploadFile"
        style="display: none"
      />
      <!-- <el-col :span="12">
        <div class="demo-basic--circle">
          <div class="block">
            <el-avatar
              shape="square"
              :size="photo.size"
              :src="photo.squareUrl"
            ></el-avatar>
          </div>
        </div>
      </el-col> -->
      <el-button @click="chooseFile">Edit Avatar</el-button>
    </el-aside>

    <el-main>
      <div>
        <!-- 个人信息展示 -->
        <el-descriptions
          class="margin-top"
          title="UserInformation"
          :column="3"
          border
        >
          <template v-slot:extra>
            <el-button
              type="primary"
              size="small"
              @click="modifyFormVisible = true"
              >Modify</el-button
            >
          </template>
          <el-descriptions-item>
            <template v-slot:label>
              <i class="el-icon-user"></i>
              UserName
            </template>
            {{ userInfo.name }}
          </el-descriptions-item>
          <el-descriptions-item>
            <template v-slot:label>
              <i class="el-icon-mobile-phone"></i>
              Tel
            </template>
            {{ userInfo.phone }}
          </el-descriptions-item>
          <el-descriptions-item>
            <template v-slot:label>
              <i class="el-icon-location-outline"></i>
              Type
            </template>
            {{ currentUserType.value }}
          </el-descriptions-item>
          <el-descriptions-item>
            <template v-slot:label> Description </template>
            {{ userInfo.description }}
          </el-descriptions-item>
          <!-- 密码不显示 -->
          <el-descriptions-item v-show="false">
            <template v-slot:label>
              <i class="el-icon-office-building"></i>
              Password
            </template>
            {{ userInfo.password }}
          </el-descriptions-item>
        </el-descriptions>

        <template v-if="isDriverUser">
          <br />
          <el-descriptions class="margin-top" title="DriverProfile" :column="2" border>
            <el-descriptions-item>
              <template v-slot:label> Work Status </template>
              {{ userInfo.driverWorkStatus === "rest" ? "休息中" : "在线接单" }}
            </el-descriptions-item>
            <el-descriptions-item>
              <template v-slot:label> Service Area </template>
              {{ userInfo.driverServiceArea || modifyData.driverServiceArea || "全城接单" }}
            </el-descriptions-item>
            <el-descriptions-item>
              <template v-slot:label> Vehicle </template>
              {{ userInfo.driverVehicle || modifyData.driverVehicle || "-" }}
            </el-descriptions-item>
            <el-descriptions-item>
              <template v-slot:label> ID Card </template>
              {{
                userInfo.driverIdCard || modifyData.driverIdCard
                  ? `${String(userInfo.driverIdCard || modifyData.driverIdCard).slice(0, 4)}********${String(userInfo.driverIdCard || modifyData.driverIdCard).slice(-4)}`
                  : "-"
              }}
            </el-descriptions-item>
            <el-descriptions-item :span="2">
              <template v-slot:label> Emergency Contact </template>
              {{ userInfo.driverEmergencyContact || modifyData.driverEmergencyContact || "-" }}
            </el-descriptions-item>
          </el-descriptions>
        </template>

        <br />
        <!-- 地址展示 -->
        <el-descriptions class="margin-top" title="Address" :column="3" border>
        </el-descriptions>
        <el-button
          type="primary"
          size="small"
          @click="modifyAddressVisible = true"
          >Add Address</el-button
        >
        <!-- Address Table -->
        <el-table :data="addressData" style="width: 100%">
          <el-table-column label="Address" width="1200">
            <template v-slot="{ row }">
              {{ row.location }}
            </template>
          </el-table-column>
          <el-table-column fixed="right" label="Operations" width="120">
            <template v-slot="scope">
              <el-button
                link
                type="primary"
                size="small"
                @click="handleDelete(scope.row)"
                >Delete</el-button
              >
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-main>
  </el-container>
</template>

<style lang="less" scoped>
.preview-image {
  width: 200px;
  height: 200px;
  object-fit: cover;
  cursor: pointer;
}

.upload-icon {
  cursor: pointer;
  width: 50px;
  height: 50px;
}
</style>
