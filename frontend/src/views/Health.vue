<template>
  <div class="health">
    <h1>健康监测</h1>
    <div class="pet-selector">
      <label for="petId">选择宠物：</label>
      <select id="petId" v-model="selectedPetId" @change="fetchHealthData">
        <option value="">请选择宠物</option>
        <option v-for="pet in pets" :key="pet.id" :value="pet.id">
          {{ pet.name }}
        </option>
      </select>
    </div>

    <div v-if="selectedPetId" class="health-content">
      <div class="health-form">
        <h2>记录健康数据</h2>
        <form @submit.prevent="recordHealthData">
          <div class="form-group">
            <label for="weight">体重（kg）</label>
            <input type="number" id="weight" v-model="healthData.weight" step="0.1" required>
          </div>
          <div class="form-group">
            <label for="mentalState">精神状态</label>
            <select id="mentalState" v-model="healthData.mentalState" required>
              <option value="excellent">优秀</option>
              <option value="good">良好</option>
              <option value="normal">正常</option>
              <option value="poor">不佳</option>
            </select>
          </div>
          <div class="form-group">
            <label for="defecation">排便情况</label>
            <select id="defecation" v-model="healthData.defecation" required>
              <option value="normal">正常</option>
              <option value="soft">偏软</option>
              <option value="hard">偏硬</option>
              <option value="diarrhea">腹泻</option>
            </select>
          </div>
          <button type="submit">保存</button>
        </form>
      </div>

      <div class="health-charts">
        <h2>健康趋势</h2>
        <div id="weightChart" ref="weightChart" style="width: 100%; height: 400px;"></div>
        <div v-if="healthAlerts.length > 0" class="health-alerts">
          <h3>健康预警</h3>
          <div v-for="alert in healthAlerts" :key="alert.id" class="alert">
            <p>{{ alert.message }}</p>
            <p class="alert-date">{{ alert.date }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import axios from 'axios'
import * as echarts from 'echarts'

const pets = ref([])
const selectedPetId = ref('')
const healthData = ref({
  weight: '',
  mentalState: 'normal',
  defecation: 'normal'
})
const healthAlerts = ref([])
const weightChart = ref(null)
let chartInstance = null

const fetchPets = async () => {
  try {
    const response = await axios.get('/api/pets')
    pets.value = response.data
  } catch (error) {
    console.error('获取宠物列表失败', error)
  }
}

const recordHealthData = async () => {
  try {
    await axios.post('/api/health-records', {
      petId: selectedPetId.value,
      ...healthData.value
    })
    fetchHealthData()
    healthData.value = {
      weight: '',
      mentalState: 'normal',
      defecation: 'normal'
    }
  } catch (error) {
    console.error('记录健康数据失败', error)
  }
}

const fetchHealthData = async () => {
  if (!selectedPetId.value) return
  
  try {
    const [recordsResponse, alertsResponse] = await Promise.all([
      axios.get(`/api/health-records/${selectedPetId.value}`),
      axios.get(`/api/health-alerts/${selectedPetId.value}`)
    ])
    
    const records = recordsResponse.data
    healthAlerts.value = alertsResponse.data
    
    // 渲染体重趋势图
    renderWeightChart(records)
  } catch (error) {
    console.error('获取健康数据失败', error)
  }
}

const renderWeightChart = (records) => {
  if (!weightChart.value) return
  
  if (chartInstance) {
    chartInstance.dispose()
  }
  
  chartInstance = echarts.init(weightChart.value)
  
  const dates = records.map(record => record.date)
  const weights = records.map(record => record.weight)
  
  const option = {
    title: {
      text: '体重变化趋势'
    },
    xAxis: {
      type: 'category',
      data: dates
    },
    yAxis: {
      type: 'value',
      name: '体重 (kg)'
    },
    series: [{
      data: weights,
      type: 'line',
      smooth: true
    }]
  }
  
  chartInstance.setOption(option)
}

onMounted(() => {
  fetchPets()
})

watch(selectedPetId, (newId) => {
  if (newId) {
    fetchHealthData()
  }
})
</script>

<style scoped>
.health {
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

.health-content {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 30px;
}

.health-form {
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.health-form h2 {
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

button {
  padding: 10px 20px;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  margin-top: 10px;
}

button:hover {
  background-color: #45a049;
}

.health-charts {
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.health-charts h2 {
  color: #333;
  margin-bottom: 20px;
}

.health-alerts {
  margin-top: 30px;
}

.health-alerts h3 {
  color: #f44336;
  margin-bottom: 15px;
}

.alert {
  background-color: #ffebee;
  padding: 10px;
  border-radius: 4px;
  margin-bottom: 10px;
  border-left: 4px solid #f44336;
}

.alert p {
  margin: 5px 0;
  color: #c62828;
}

.alert-date {
  font-size: 12px;
  color: #888;
}
</style>