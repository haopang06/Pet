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
            <label for="recordDate">记录日期</label>
            <input type="date" id="recordDate" v-model="healthData.date" :max="today" required>
          </div>
          <div class="form-group">
            <label for="weight">体重（kg）</label>
            <input type="number" id="weight" v-model.number="healthData.weight" min="0.1" step="0.1" required>
          </div>
          <div class="form-group">
            <label for="waterIntake">饮水量（ml/天）</label>
            <input type="number" id="waterIntake" v-model.number="healthData.waterIntake" min="0" step="1" required>
          </div>
          <div class="form-group">
            <label for="foodIntake">进食量（g/天）</label>
            <input type="number" id="foodIntake" v-model.number="healthData.foodIntake" min="0" step="1" required>
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
        <h2>最近一周健康数据</h2>
        <div v-if="hasRecentHealthData" id="weightChart" ref="weightChart" class="chart"></div>
        <div v-else class="health-empty">
          <div class="empty-illustration" aria-hidden="true">
            <span></span>
          </div>
          <h3>还没有健康数据</h3>
          <p>记录一次体重、饮水量和进食量后，这里会展示最近一周趋势。</p>
        </div>

        <div v-if="weeklyWarning" class="weekly-warning">
          该宠物最近一周状态不佳，请多加留意
        </div>

        <div class="weekly-summary" aria-label="最近一周状态灯">
          <div v-for="day in recentWeekData" :key="day.date" class="summary-item" :class="day.statusClass">
            <strong>{{ day.shortDate }}</strong>
            <span><i class="state-dot"></i>{{ day.statusLabel }}</span>
            <small>体重：{{ day.weightLabel }}</small>
          </div>
        </div>

        <div v-if="healthAlerts.length > 0" class="health-alerts">
          <h3>健康预警</h3>
          <div
            v-for="alert in healthAlerts"
            :key="alert.id"
            class="alert"
            :class="{ handled: isAlertHandled(alert), clickable: !isAlertHandled(alert) }"
            role="button"
            :tabindex="isAlertHandled(alert) ? -1 : 0"
            @click="markAlertHandled(alert)"
            @keydown.enter.prevent="markAlertHandled(alert)"
          >
            <p>{{ alert.message }}</p>
            <p class="alert-date">{{ alert.date }}</p>
            <div class="alert-actions">
              <button
                v-if="!isAlertHandled(alert)"
                type="button"
                :disabled="handlingAlertId === alert.id"
                @click.stop="markAlertHandled(alert)"
              >
                {{ handlingAlertId === alert.id ? '处理中' : '标记为已处理' }}
              </button>
              <span v-else>已处理/已好转</span>
              <a
                href="https://www.google.com/maps/search/%E5%AE%A0%E7%89%A9%E5%8C%BB%E9%99%A2"
                target="_blank"
                rel="noreferrer"
                @click.stop
              >
                查看附近宠物医院
              </a>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import axios from 'axios'
import * as echarts from 'echarts'

const pets = ref([])
const selectedPetId = ref('')
const today = formatDate(new Date())
const healthData = ref(createEmptyHealthData())
const healthAlerts = ref([])
const recentWeekData = ref([])
const weeklyWarning = ref(false)
const weightChart = ref(null)
const handlingAlertId = ref(null)
let chartInstance = null

const mentalStateLabels = {
  excellent: '优秀',
  good: '良好',
  normal: '正常',
  poor: '不佳'
}

const defecationLabels = {
  normal: '正常',
  soft: '偏软',
  hard: '偏硬',
  diarrhea: '腹泻'
}

function createEmptyHealthData() {
  return {
    date: formatDate(new Date()),
    weight: '',
    waterIntake: '',
    foodIntake: '',
    mentalState: 'normal',
    defecation: 'normal'
  }
}

const hasRecentHealthData = computed(() => recentWeekData.value.some(day => day.statusClass !== 'empty'))

function formatDate(date) {
  const value = new Date(date)
  const year = value.getFullYear()
  const month = String(value.getMonth() + 1).padStart(2, '0')
  const day = String(value.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const getCurrentUserId = () => {
  const userId = Number(localStorage.getItem('userId'))
  return Number.isFinite(userId) && userId > 0 ? userId : null
}

const getUserParams = () => {
  const userId = getCurrentUserId()
  return userId ? { userId } : {}
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

const recordHealthData = async () => {
  try {
    await axios.post('/api/health-records', {
      petId: Number(selectedPetId.value),
      date: healthData.value.date,
      weight: Number(healthData.value.weight),
      waterIntake: Number(healthData.value.waterIntake),
      foodIntake: Number(healthData.value.foodIntake),
      mentalState: healthData.value.mentalState,
      defecation: healthData.value.defecation
    })
    await fetchHealthData()
    healthData.value = createEmptyHealthData()
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

    const weekData = buildRecentWeekData(recordsResponse.data)
    recentWeekData.value = weekData
    healthAlerts.value = normalizeAlerts(alertsResponse.data)
    weeklyWarning.value = weekData.filter(day => day.abnormal).length >= 3

    await nextTick()
    if (weekData.some(day => day.statusClass !== 'empty')) {
      renderWeightChart(weekData)
    } else if (chartInstance) {
      chartInstance.dispose()
      chartInstance = null
    }
  } catch (error) {
    console.error('获取健康数据失败', error)
  }
}

const normalizeAlerts = (alerts) => {
  return (alerts || []).map(alert => ({
    ...alert,
    handled: normalizeHandled(alert.handled)
  }))
}

const normalizeHandled = (value) => value === true || value === 1 || value === '1' || value === 'true'

const isAlertHandled = (alert) => normalizeHandled(alert?.handled)

const markAlertHandled = async (alert) => {
  if (!alert?.id || isAlertHandled(alert) || handlingAlertId.value === alert.id) return

  const previousAlerts = healthAlerts.value.map(item => ({ ...item }))
  try {
    handlingAlertId.value = alert.id
    healthAlerts.value = healthAlerts.value.map(item => (
      item.id === alert.id ? { ...item, handled: true } : item
    ))
    const response = await axios.post(`/api/health-alerts/${alert.id}/handled`)
    healthAlerts.value = healthAlerts.value.map(item => (
      item.id === alert.id ? { ...item, ...response.data, handled: true } : item
    ))
    await fetchHealthData()
  } catch (error) {
    console.error('标记预警失败', error)
    healthAlerts.value = previousAlerts
  } finally {
    handlingAlertId.value = null
  }
}

const buildRecentWeekData = (records) => {
  const dateKeys = getRecentWeekDateKeys()
  const recordsByDate = records.reduce((map, record) => {
    const key = String(record.date).slice(0, 10)
    if (!map[key]) {
      map[key] = []
    }
    map[key].push(record)
    return map
  }, {})

  return dateKeys.map(date => {
    const dayRecords = recordsByDate[date] || []
    if (dayRecords.length === 0) {
      return {
        date,
        shortDate: date.slice(5),
        weight: null,
        weightLabel: '无',
        waterIntakeLabel: '无',
        foodIntakeLabel: '无',
        mentalStateLabel: '无',
        defecationLabel: '无',
        statusLabel: '无',
        statusClass: 'empty',
        abnormal: false
      }
    }

    const latestRecord = dayRecords[dayRecords.length - 1]
    const abnormal = dayRecords.some(isAbnormalRecord)

    return {
      date,
      shortDate: date.slice(5),
      weight: Number(latestRecord.weight),
      weightLabel: `${Number(latestRecord.weight).toFixed(1)}kg`,
      waterIntakeLabel: formatIntake(latestRecord.waterIntake, 'ml'),
      foodIntakeLabel: formatIntake(latestRecord.foodIntake, 'g'),
      mentalStateLabel: mentalStateLabels[latestRecord.mentalState] || latestRecord.mentalState,
      defecationLabel: defecationLabels[latestRecord.defecation] || latestRecord.defecation,
      statusLabel: abnormal ? '异常' : '正常',
      statusClass: abnormal ? 'abnormal' : 'normal',
      abnormal
    }
  })
}

const getRecentWeekDateKeys = () => {
  const dates = []
  const baseDate = new Date(today)
  for (let offset = 6; offset >= 0; offset--) {
    const date = new Date(baseDate)
    date.setDate(baseDate.getDate() - offset)
    dates.push(formatDate(date))
  }
  return dates
}

const isAbnormalRecord = (record) => {
  return record.mentalState === 'poor' || record.defecation !== 'normal'
}

const formatIntake = (value, unit) => {
  const numberValue = Number(value)
  return Number.isFinite(numberValue) ? `${numberValue.toFixed(0)}${unit}` : '无'
}

const renderWeightChart = (weekData) => {
  if (!weightChart.value) return

  if (chartInstance) {
    chartInstance.dispose()
  }

  chartInstance = echarts.init(weightChart.value)

  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: params => {
        const index = params[0].dataIndex
        const day = weekData[index]
        if (!day || day.statusLabel === '无') {
          return `${day?.date || ''}<br/>无健康数据`
        }
        return [
          day.date,
          `体重：${day.weightLabel}`,
          `饮水量：${day.waterIntakeLabel}`,
          `进食量：${day.foodIntakeLabel}`,
          `精神状态：${day.mentalStateLabel}`,
          `排便情况：${day.defecationLabel}`,
          `综合状态：${day.statusLabel}`
        ].join('<br/>')
      }
    },
    legend: {
      data: ['体重']
    },
    grid: {
      left: 48,
      right: 24,
      top: 44,
      bottom: 36
    },
    xAxis: {
      type: 'category',
      data: weekData.map(day => day.shortDate)
    },
    yAxis: {
      type: 'value',
      name: '体重 (kg)',
      min: value => Math.max(0, Math.floor(value.min - 1)),
      splitLine: {
        lineStyle: {
          color: '#e8eee9'
        }
      }
    },
    series: [
      {
        name: '体重',
        data: weekData.map(day => day.weight),
        type: 'line',
        smooth: true,
        connectNulls: false,
        lineStyle: {
          color: '#2e7d52',
          width: 3
        },
        itemStyle: {
          color: '#2e7d52'
        },
        areaStyle: {
          color: 'rgba(46, 125, 82, 0.08)'
        },
        symbolSize: 8,
        label: {
          show: true,
          formatter: params => params.value == null ? '' : `${Number(params.value).toFixed(1)}kg`
        }
      }
    ]
  }

  chartInstance.setOption(option)
}

onMounted(() => {
  fetchPets()
})

watch(selectedPetId, (newId) => {
  if (newId) {
    fetchHealthData()
  } else {
    recentWeekData.value = []
    weeklyWarning.value = false
    if (chartInstance) {
      chartInstance.dispose()
      chartInstance = null
    }
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
  padding: 26px;
  border-radius: 8px;
  border: 1px solid #e0e7e3;
}

.health-form h2 {
  color: #333;
  margin-bottom: 24px;
}

.form-group {
  margin-bottom: 22px;
}

label {
  display: block;
  margin-bottom: 8px;
  color: #666;
}

input, select {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 16px;
}

button {
  padding: 12px 20px;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  margin-top: 4px;
}

button:hover {
  background-color: #45a049;
}

.health-charts {
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  border: 1px solid #e0e7e3;
}

.health-charts h2 {
  color: #333;
  margin-bottom: 20px;
}

.chart {
  width: 100%;
  height: 400px;
}

.health-empty {
  display: grid;
  place-items: center;
  min-height: 320px;
  padding: 28px;
  border: 1px dashed #cdd8d1;
  border-radius: 8px;
  background-color: #f7faf8;
  text-align: center;
  color: #66766c;
}

.health-empty h3 {
  color: #1f2d26;
  margin: 12px 0 8px;
}

.health-empty p {
  max-width: 360px;
  line-height: 1.6;
}

.empty-illustration {
  position: relative;
  width: 96px;
  height: 78px;
  border: 3px solid #8bb99e;
  border-radius: 44px 44px 28px 28px;
  background-color: #eef8f2;
}

.empty-illustration::before,
.empty-illustration::after {
  position: absolute;
  content: '';
  top: 24px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background-color: #2e7d52;
}

.empty-illustration::before {
  left: 28px;
}

.empty-illustration::after {
  right: 28px;
}

.empty-illustration span {
  position: absolute;
  left: 50%;
  bottom: 22px;
  width: 24px;
  height: 10px;
  border-bottom: 3px solid #2e7d52;
  border-radius: 0 0 20px 20px;
  transform: translateX(-50%);
}

.weekly-warning {
  margin-top: 18px;
  padding: 12px 14px;
  border-left: 4px solid #f44336;
  background-color: #ffebee;
  color: #c62828;
  border-radius: 4px;
  font-weight: 600;
}

.weekly-summary {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  gap: 8px;
  margin-top: 18px;
}

.summary-item {
  padding: 11px 8px;
  border-radius: 8px;
  background-color: #f5f5f5;
  text-align: center;
  color: #555;
  border: 1px solid transparent;
}

.summary-item strong,
.summary-item span,
.summary-item small {
  display: block;
}

.summary-item span {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  margin-top: 4px;
}

.summary-item small {
  margin-top: 4px;
  color: #777;
}

.summary-item.normal {
  background-color: #e8f5e9;
  border-color: #c8e6c9;
  color: #2e7d32;
}

.summary-item.abnormal {
  background-color: #ffebee;
  border-color: #ffcdd2;
  color: #c62828;
}

.summary-item.empty {
  background-color: #eeeeee;
  border-color: #e0e0e0;
  color: #777;
}

.state-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: currentColor;
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

.alert.clickable {
  cursor: pointer;
}

.alert.clickable:hover {
  background-color: #ffe1e6;
}

.alert.handled {
  background-color: #f3f6f4;
  border-left-color: #9aa8a0;
}

.alert p {
  margin: 5px 0;
  color: #c62828;
}

.alert.handled p {
  color: #66766c;
}

.alert-date {
  font-size: 12px;
  color: #888;
}

.alert-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: 10px;
}

.alert-actions button,
.alert-actions a,
.alert-actions span {
  display: inline-flex;
  align-items: center;
  min-height: 30px;
  padding: 6px 10px;
  border-radius: 6px;
  font-size: 13px;
}

.alert-actions button {
  margin-top: 0;
}

.alert-actions button:disabled {
  background-color: #9aa8a0;
  cursor: not-allowed;
}

.alert-actions a {
  color: #2e7d52;
  background-color: #edf7ef;
  text-decoration: none;
}

.alert-actions span {
  color: #5b6a61;
  background-color: #e9efeb;
}

@media (max-width: 900px) {
  .health-content {
    grid-template-columns: 1fr;
  }

  .weekly-summary {
    grid-template-columns: repeat(auto-fit, minmax(90px, 1fr));
  }
}
</style>
