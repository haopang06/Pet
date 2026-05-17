<template>
  <div class="pets">
    <h1>宠物信息管理</h1>
    <button class="add-btn" @click="showAddForm = true">添加宠物</button>
    
    <div v-if="showAddForm" class="add-form">
      <h2>添加宠物</h2>
      <form @submit.prevent="addPet">
        <div class="form-group">
          <label for="name">宠物名称</label>
          <input type="text" id="name" v-model="newPet.name" required>
        </div>
        <div class="form-group">
          <label for="breed">品种</label>
          <input type="text" id="breed" v-model="newPet.breed" required>
        </div>
        <div class="form-group">
          <label for="age">年龄（岁）</label>
          <input type="number" id="age" v-model="newPet.age" required>
        </div>
        <div class="form-group">
          <label for="weight">体重（kg）</label>
          <input type="number" id="weight" v-model="newPet.weight" step="0.1" required>
        </div>
        <div class="form-group">
          <label for="activityLevel">活动量</label>
          <select id="activityLevel" v-model="newPet.activityLevel" required>
            <option value="low">低</option>
            <option value="medium">中</option>
            <option value="high">高</option>
          </select>
        </div>
        <button type="submit">保存</button>
        <button type="button" @click="showAddForm = false">取消</button>
      </form>
    </div>

    <div class="pet-list">
      <div v-for="pet in pets" :key="pet.id" class="pet-card">
        <h2>{{ pet.name }}</h2>
        <p>品种: {{ pet.breed }}</p>
        <p>年龄: {{ pet.age }}岁</p>
        <p>体重: {{ pet.weight }}kg</p>
        <p>活动量: {{ pet.activityLevel }}</p>
        <button @click="editPet(pet)">编辑</button>
        <button @click="deletePet(pet.id)">删除</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const showAddForm = ref(false)
const pets = ref([])
const newPet = ref({
  name: '',
  breed: '',
  age: '',
  weight: '',
  activityLevel: 'medium'
})

const fetchPets = async () => {
  try {
    const response = await axios.get('/api/pets')
    pets.value = response.data
  } catch (error) {
    console.error('获取宠物列表失败', error)
  }
}

const addPet = async () => {
  try {
    await axios.post('/api/pets', newPet.value)
    fetchPets()
    showAddForm.value = false
    newPet.value = {
      name: '',
      breed: '',
      age: '',
      weight: '',
      activityLevel: 'medium'
    }
  } catch (error) {
    console.error('添加宠物失败', error)
  }
}

const editPet = (pet) => {
  // 编辑功能实现
  console.log('编辑宠物', pet)
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

.add-form {
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  margin-bottom: 30px;
}

.add-form h2 {
  color: #333;
  margin-bottom: 20px;
}

.form-group {
  margin-bottom: 15px;
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

.add-form button {
  padding: 10px 20px;
  margin-right: 10px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.add-form button[type="submit"] {
  background-color: #4CAF50;
  color: white;
}

.add-form button[type="button"] {
  background-color: #f44336;
  color: white;
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
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.pet-card h2 {
  color: #333;
  margin-bottom: 10px;
}

.pet-card p {
  color: #666;
  margin-bottom: 5px;
}

.pet-card button {
  padding: 5px 10px;
  margin-right: 10px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  margin-top: 10px;
}

.pet-card button:first-of-type {
  background-color: #2196F3;
  color: white;
}

.pet-card button:last-of-type {
  background-color: #f44336;
  color: white;
}
</style>