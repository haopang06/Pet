<template>
  <div class="pets">
    <section class="page-hero">
      <div class="hero-title">
        <span class="hero-mark"></span>
        <div>
          <p>Pet Profile Center</p>
          <h1>宠物信息管理</h1>
        </div>
      </div>
      <div class="hero-summary">
        <PetAvatar :pet="pets[0] || {}" :type="resolvePetType(pets[0] || {})" size="sm" circle />
        <span>{{ pets.length > 0 ? `${pets.length} 位成员` : '等待添加成员' }}</span>
      </div>
    </section>

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
          <div class="form-group photo-group">
            <label>宠物照片</label>
            <div class="photo-upload">
              <PetAvatar :pet="petForm" :type="petForm.petType" size="lg" />
              <div class="photo-actions">
                <input id="petPhotoInput" ref="photoInput" class="file-input" type="file" accept="image/*" @change="handlePhotoChange">
                <label class="upload-btn" for="petPhotoInput">上传照片</label>
                <button v-if="petForm.photo" type="button" class="ghost-btn" @click="removePhoto">移除照片</button>
                <p v-if="photoStatus" class="photo-status">{{ photoStatus }}</p>
              </div>
            </div>
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
          <PetAvatar :pet="pet" :type="resolvePetType(pet)" />
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
      <button type="button" class="add-pet-card" @click="openCreateForm">
        <strong>+</strong>
        <span>添加新成员，开启个性化喂养方案</span>
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import PetAvatar from '../components/PetAvatar.vue'
import { fileToCompressedDataUrl } from '../utils/image'

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
  activityLevel: 'medium',
  photo: ''
})

const showPetForm = ref(false)
const pets = ref([])
const errorMessage = ref('')
const loginNotice = ref(false)
const editingPetId = ref(null)
const petForm = ref(createEmptyPet())
const photoInput = ref(null)
const photoStatus = ref('')
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
  photoStatus.value = ''
  loginNotice.value = false
  showPetForm.value = true
}

const closeForm = () => {
  showPetForm.value = false
  editingPetId.value = null
  petForm.value = createEmptyPet()
  errorMessage.value = ''
  photoStatus.value = ''
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
    activityLevel: petForm.value.activityLevel,
    photo: petForm.value.photo || ''
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

    let response
    if (isEditing.value) {
      response = await axios.put(`/api/pets/${editingPetId.value}`, payload, config)
      pets.value = pets.value.map(pet => pet.id === editingPetId.value ? response.data : pet)
    } else {
      response = await axios.post('/api/pets', payload, config)
      pets.value = [...pets.value, response.data]
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
    activityLevel: pet.activityLevel || 'medium',
    photo: pet.photo || ''
  }
  errorMessage.value = ''
  photoStatus.value = ''
  showPetForm.value = true
}

const handlePhotoChange = async (event) => {
  const file = event.target.files?.[0]
  if (!file) return

  try {
    photoStatus.value = '照片处理中...'
    petForm.value.photo = await fileToCompressedDataUrl(file, 480, 0.68)
    if (isEditing.value) {
      await savePetPhoto()
    } else {
      photoStatus.value = '照片已选择，保存宠物后生效'
    }
  } catch (error) {
    errorMessage.value = error.message || '照片读取失败'
    photoStatus.value = ''
  } finally {
    event.target.value = ''
  }
}

const savePetPhoto = async () => {
  if (!isEditing.value) return

  const response = await axios.patch(`/api/pets/${editingPetId.value}/photo`, {
    photo: petForm.value.photo || ''
  })
  pets.value = pets.value.map(pet => pet.id === editingPetId.value ? response.data : pet)
  photoStatus.value = petForm.value.photo ? '照片已保存' : '照片已移除'
}

const removePhoto = async () => {
  petForm.value.photo = ''
  if (isEditing.value) {
    try {
      photoStatus.value = '正在移除照片...'
      await savePetPhoto()
    } catch (error) {
      errorMessage.value = error.response?.data?.message || '照片移除失败'
      photoStatus.value = ''
    }
  } else {
    photoStatus.value = ''
  }
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
  color: #1f2d26;
  font-size: 28px;
  line-height: 1.2;
}

.page-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  flex-wrap: wrap;
  margin-bottom: 24px;
  padding: 18px;
  border: 1px solid #e0e7e3;
  border-radius: 12px;
  background: linear-gradient(135deg, #ffffff 0%, #f7faf8 100%);
}

.hero-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.hero-title p {
  color: #60756a;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.08em;
  margin-bottom: 4px;
}

.hero-mark {
  width: 12px;
  height: 42px;
  border-radius: 999px;
  background: linear-gradient(180deg, #10b981, #3b82f6);
}

.hero-summary {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  min-height: 44px;
  padding: 5px 14px 5px 8px;
  border: 1px solid #dce5df;
  border-radius: 12px;
  background-color: rgba(255, 255, 255, 0.88);
  color: #25332b;
  font-size: 14px;
  font-weight: 800;
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

.photo-group {
  grid-column: span 3;
}

.photo-upload {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px;
  border: 1px dashed #cdd8d1;
  border-radius: 8px;
  background-color: #f7faf8;
}

.photo-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.file-input {
  position: absolute;
  width: 1px;
  height: 1px;
  overflow: hidden;
  clip: rect(0 0 0 0);
  white-space: nowrap;
}

.pet-form .photo-actions button,
.pet-form .photo-actions .upload-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-right: 0;
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
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

.pet-form .photo-actions button[type="button"] {
  background-color: #2e7d52;
  color: white;
}

.pet-form .photo-actions .upload-btn {
  margin-bottom: 0;
  background-color: #2e7d52;
  color: white;
}

.pet-form .photo-actions .ghost-btn[type="button"] {
  background-color: #e9efeb;
  color: #4f6358;
}

.photo-status {
  flex-basis: 100%;
  margin: 0;
  color: #60756a;
  font-size: 13px;
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

.pet-card,
.add-pet-card {
  min-height: 214px;
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

.add-pet-card {
  display: grid;
  place-items: center;
  align-content: center;
  gap: 10px;
  border: 2px dashed #cbd6cf;
  background-color: #fbfcfb;
  color: #7a8980;
  cursor: pointer;
  text-align: center;
}

.add-pet-card:hover {
  border-color: #8bb99e;
  background-color: #f3faf6;
  color: #2e7d52;
}

.add-pet-card strong {
  display: grid;
  place-items: center;
  width: 54px;
  height: 54px;
  border-radius: 50%;
  background-color: #edf2ef;
  font-size: 38px;
  line-height: 1;
  font-weight: 400;
}

.add-pet-card span {
  max-width: 210px;
  line-height: 1.5;
  font-size: 15px;
}

@media (max-width: 760px) {
  .form-grid,
  .photo-group {
    grid-template-columns: 1fr;
    grid-column: span 1;
  }
}
</style>
