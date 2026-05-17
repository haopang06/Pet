<template>
  <div class="feeding">
    <h1>智能喂养方案</h1>
    <div class="pet-selector">
      <label for="petId">选择宠物：</label>
      <select id="petId" v-model="selectedPetId" @change="generateFeedingPlan">
        <option value="">请选择宠物</option>
        <option v-for="pet in pets" :key="pet.id" :value="pet.id">
          {{ pet.name }}
        </option>
      </select>
    </div>

    <div v-if="feedingPlan" class="feeding-plan">
      <h2>{{ selectedPetName }}的喂养方案</h2>
      <div class="plan-details">
        <div class="plan-item">
          <h3>每日能量需求</h3>
          <p>{{ feedingPlan.dailyEnergy }} kcal</p>
        </div>
        <div class="plan-item">
          <h3>食物配比</h3>
          <p>蛋白质: {{ feedingPlan.protein }}%</p>
          <p>脂肪: {{ feedingPlan.fat }}%</p>
          <p>碳水化合物: {{ feedingPlan.carbs }}%</p>
        </div>
        <div class="plan-item">
          <h3>喂食频次</h3>
          <p>{{ feedingPlan.frequency }} 次/天</p>
        </div>
        <div class="plan-item">
          <h3>每次食量</h3>
          <p>{{ feedingPlan.portionsize }} g/次</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import axios from 'axios'

const pets = ref([])
const selectedPetId = ref('')
const feedingPlan = ref(null)

const selectedPetName = computed(() => {
  const pet = pets.value.find(p => p.id == selectedPetId.value)
  return pet ? pet.name : ''
})

const fetchPets = async () => {
  try {
    const response = await axios.get('/api/pets')
    pets.value = response.data
  } catch (error) {
    console.error('获取宠物列表失败', error)
  }
}

const generateFeedingPlan = async () => {
  if (!selectedPetId.value) {
    feedingPlan.value = null
    return
  }
  
  try {
    const response = await axios.get(`/api/feeding-plan/${selectedPetId.value}`)
    feedingPlan.value = response.data
  } catch (error) {
    console.error('生成喂养方案失败', error)
  }
}

onMounted(() => {
  fetchPets()
})
</script>

<style scoped>
.feeding {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

h1 {
  color: #333;
  margin-bottom: 20px;
}

.pet-selector {
  margin-bottom: 30px;
}

.pet-selector label {
  margin-right: 10px;
  font-size: 16px;
  color: #333;
}

.pet-selector select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 16px;
}

.feeding-plan {
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.feeding-plan h2 {
  color: #333;
  margin-bottom: 20px;
}

.plan-details {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
}

.plan-item {
  background-color: #f5f5f5;
  padding: 15px;
  border-radius: 4px;
}

.plan-item h3 {
  color: #4CAF50;
  margin-bottom: 10px;
  font-size: 14px;
}

.plan-item p {
  color: #666;
  margin-bottom: 5px;
  font-size: 16px;
}
</style>