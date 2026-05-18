<template>
  <div class="pets">
    <h1>宠物信息管理</h1>
    <button class="add-btn" @click="openCreateForm">添加宠物</button>

    <div v-if="loginNotice" class="login-notice">
      <span>还没有用户登录系统</span>
      <button type="button" @click="goLogin">去登录</button>
    </div>

    <div v-if="showPetForm" class="pet-form">
      <h2>{{ isEditing ? '编辑宠物' : '添加宠物' }}</h2>
      <form @submit.prevent="savePet">
        <div class="form-grid">
          <div class="form-group">
            <label for="name">宠物名称</label>
            <input type="text" id="name" v-model.trim="petForm.name" required>
          </div>
          <div class="form-group">
            <label for="petType">宠物类型</label>
            <select id="petType" v-model="petForm.petType" required>
              <option value="cat">猫</option>
              <option value="dog">狗</option>
            </select>
          </div>
          <div class="form-group">
            <label for="breed">品种</label>
            <select id="breed" v-model="petForm.breed" required>
              <option v-for="breed in currentBreedOptions" :key="breed" :value="breed">
                {{ breed }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label for="age">年龄（岁）</label>
            <input type="number" id="age" v-model.number="petForm.age" min="0" required>
          </div>
          <div class="form-group">
            <label for="weight">体重（kg）</label>
            <input type="number" id="weight" v-model.number="petForm.weight" min="0.1" step="0.1" required>
          </div>
          <div class="form-group">
            <label for="activityLevel">活动量</label>
            <select id="activityLevel" v-model="petForm.activityLevel" required>
              <option value="low">低</option>
              <option value="medium">中</option>
              <option value="high">高</option>
            </select>
          </div>
        </div>
        <div class="form-actions">
          <button type="submit">{{ isEditing ? '保存修改' : '保存' }}</button>
          <button type="button" @click="closeForm">取消</button>
        </div>
      </form>
      <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>
    </div>

    <div class="pet-list">
      <div v-for="pet in pets" :key="pet.id" class="pet-card">
        <div class="pet-card-head">
          <PetTypeIcon :type="resolvePetType(pet)" />
          <div>
            <h2>{{ pet.name }}</h2>
            <span>{{ petTypeText(resolvePetType(pet)) }} / {{ pet.breed }}</span>
          </div>
        </div>
        <div class="pet-card-meta">
          <p><span>年龄</span><strong>{{ pet.age }}岁</strong></p>
          <p><span>体重</span><strong>{{ formatWeight(pet.weight) }}</strong></p>
          <p class="activity-meta">
            <span>活动量</span>
            <strong class="activity-badge" :class="activityLevelClass(pet.activityLevel)">
              {{ activityLevelText(pet.activityLevel) }}
            </strong>
          </p>
        </div>
        <div class="pet-card-actions">
          <button @click="editPet(pet)">编辑</button>
          <button @click="deletePet(pet.id)">删除</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import PetTypeIcon from '../components/PetTypeIcon.vue'

const breedOptions = {
  cat: ['美短', '英短', '金渐层', '银渐层', '布偶', '暹罗', '缅因', '加菲', '波斯', '狸花', '中华田园猫'],
  dog: ['金毛', '哈士奇', '萨摩耶', '拉布拉多', '边牧', '德牧', '阿拉斯加', '柯基', '泰迪', '比熊', '中华田园犬']
}

const createEmptyPet = () => ({
  name: '',
  petType: 'cat',
  breed: breedOptions.cat[0],
  age: '',
  weight: '',
  activityLevel: 'medium'
})

const showPetForm = ref(false)
const pets = ref([])
const errorMessage = ref('')
const loginNotice = ref(false)
const editingPetId = ref(null)
const petForm = ref(createEmptyPet())
const router = useRouter()

const isEditing = computed(() => editingPetId.value !== null)
const currentBreedOptions = computed(() => breedOptions[petForm.value.petType] || [])

watch(
  () => petForm.value.petType,
  () => {
    if (!currentBreedOptions.value.includes(petForm.value.breed)) {
      petForm.value.breed = currentBreedOptions.value[0] || ''
    }
  }
)

const getCurrentUserId = () => {
  const userId = Number(localStorage.getItem('userId'))
  return Number.isFinite(userId) && userId > 0 ? userId : null
}

const getUserParams = () => {
  const userId = getCurrentUserId()
  return userId ? { userId } : {}
}

const petTypeText = (petType) => {
  const labels = {
    cat: '猫',
    dog: '狗'
  }
  return labels[petType] || petType || '未分类'
}

const activityLevelText = (activityLevel) => {
  const labels = {
    low: '低',
    medium: '中',
    high: '高'
  }
  return labels[activityLevel] || activityLevel
}

const activityLevelClass = (activityLevel) => {
  const classes = {
    low: 'activity-low',
    medium: 'activity-medium',
    high: 'activity-high'
  }
  return classes[activityLevel] || 'activity-medium'
}

const formatWeight = (weight) => {
  const numberValue = Number(weight)
  return Number.isFinite(numberValue) ? `${numberValue.toFixed(1)}kg` : '-'
}

const inferPetType = (breed) => {
  if (breedOptions.dog.includes(breed)) {
    return 'dog'
  }
  return 'cat'
}

const resolvePetType = (pet) => {
  return pet.petType || inferPetType(pet.breed)
}

const fetchPets = async () => {
  try {
    const response = await axios.get('/api/pets', {
      params: getUserParams()
    })
    pets.value = response.data
  } catch (error) {
    console.error('获取宠物列表失败', error)
  }
}

const openCreateForm = () => {
  if (!getCurrentUserId()) {
    showPetForm.value = false
    loginNotice.value = true
    return
  }
  editingPetId.value = null
  petForm.value = createEmptyPet()
  errorMessage.value = ''
  loginNotice.value = false
  showPetForm.value = true
}

const closeForm = () => {
  showPetForm.value = false
  editingPetId.value = null
  petForm.value = createEmptyPet()
  errorMessage.value = ''
}

const goLogin = () => {
  router.push('/login')
}

const buildPayload = () => {
  const payload = {
    name: petForm.value.name,
    petType: petForm.value.petType,
    breed: petForm.value.breed,
    age: Number(petForm.value.age),
    weight: Number(petForm.value.weight),
    activityLevel: petForm.value.activityLevel
  }
  const userId = getCurrentUserId()
  if (userId) {
    payload.user = {
      id: userId
    }
  }
  return payload
}

const savePet = async () => {
  try {
    if (!getCurrentUserId()) {
      showPetForm.value = false
      loginNotice.value = true
      return
    }
    errorMessage.value = ''
    const payload = buildPayload()
    const config = {
      params: getUserParams()
    }

    if (isEditing.value) {
      await axios.put(`/api/pets/${editingPetId.value}`, payload, config)
    } else {
      await axios.post('/api/pets', payload, config)
    }

    await fetchPets()
    closeForm()
  } catch (error) {
    console.error('保存宠物失败', error)
    errorMessage.value = error.response?.data?.message || '保存宠物失败，请稍后重试'
  }
}

const editPet = (pet) => {
  const petType = pet.petType || inferPetType(pet.breed)
  const options = breedOptions[petType] || breedOptions.cat

  editingPetId.value = pet.id
  petForm.value = {
    name: pet.name,
    petType,
    breed: options.includes(pet.breed) ? pet.breed : options[0],
    age: pet.age,
    weight: pet.weight,
    activityLevel: pet.activityLevel || 'medium'
  }
  errorMessage.value = ''
  showPetForm.value = true
}

const deletePet = async (id) => {
  try {
    await axios.delete(`/api/pets/${id}`)
    fetchPets()
  } catch (error) {
    console.error('删除宠物失败', error)
  }
}

onMounted(() => {
  fetchPets()
})
</script>

<style scoped>
.pets {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

h1 {
  color: #333;
  margin-bottom: 20px;
}

.add-btn {
  background-color: #4CAF50;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  margin-bottom: 20px;
}

.add-btn:hover {
  background-color: #45a049;
}

.login-notice {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  margin-bottom: 20px;
  background-color: #fff8e1;
  border-left: 4px solid #ff9800;
  border-radius: 4px;
  color: #8a5a00;
}

.login-notice button {
  padding: 7px 12px;
  border: none;
  border-radius: 4px;
  background-color: #4CAF50;
  color: white;
  cursor: pointer;
}

.login-notice button:hover {
  background-color: #45a049;
}

.pet-form {
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  margin-bottom: 30px;
}

.pet-form h2 {
  color: #333;
  margin-bottom: 20px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  column-gap: 28px;
  row-gap: 18px;
}

.form-group {
  margin-bottom: 0;
}

label {
  display: block;
  margin-bottom: 5px;
  color: #666;
}

input, select {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 16px;
}

.form-actions {
  margin-top: 18px;
}

.pet-form button {
  padding: 10px 20px;
  margin-right: 10px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.pet-form button[type="submit"] {
  background-color: #4CAF50;
  color: white;
}

.pet-form button[type="button"] {
  background-color: #f44336;
  color: white;
}

.error-message {
  color: #f44336;
  margin-top: 10px;
  font-size: 14px;
}

.pet-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.pet-card {
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  border: 1px solid #e0e7e3;
}

.pet-card-head {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.pet-card h2 {
  color: #1f2d26;
  margin-bottom: 4px;
}

.pet-card-head span {
  color: #64746b;
  font-size: 14px;
}

.pet-card-meta {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
}

.pet-card-meta p {
  padding: 10px;
  border-radius: 6px;
  background-color: #f7faf8;
}

.pet-card-meta span,
.pet-card-meta strong {
  display: block;
}

.pet-card-meta span {
  color: #718178;
  font-size: 12px;
  margin-bottom: 4px;
}

.pet-card-meta strong {
  color: #26332c;
  font-size: 15px;
  font-weight: 700;
}

.activity-meta .activity-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: fit-content;
  min-width: 28px;
  padding: 3px 8px;
  border-radius: 999px;
  font-size: 13px;
}

.activity-meta .activity-badge.activity-low {
  background-color: #e8f2ff;
  color: #2563a6;
}

.activity-meta .activity-badge.activity-medium {
  background-color: #edf7ef;
  color: #2e7d52;
}

.activity-meta .activity-badge.activity-high {
  background-color: #fff1dc;
  color: #a15c10;
}

.pet-card-actions {
  margin-top: 14px;
}

.pet-card button {
  padding: 7px 12px;
  margin-right: 10px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
}

.pet-card button:first-of-type {
  background-color: #2196F3;
  color: white;
}

.pet-card button:last-of-type {
  background-color: #f44336;
  color: white;
}

@media (max-width: 760px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
