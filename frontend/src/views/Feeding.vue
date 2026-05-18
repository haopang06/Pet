<template>
  <div class="feeding">
    <h1>智能喂养方案</h1>
    <div class="feeding-controls">
      <button class="generate-all-btn" :disabled="pets.length === 0 || feedingLoading" @click="generateAllFeedingPlans">
        生成所有宠物喂养方案
      </button>
      <div class="pet-selector">
        <label for="petId">选择宠物：</label>
        <select id="petId" v-model="selectedPetId" @change="generateFeedingPlan">
          <option value="">请选择宠物</option>
          <option v-for="pet in pets" :key="pet.id" :value="pet.id">
            {{ pet.name }}（{{ petTypeText(pet.petType) }} / {{ pet.breed }}）
          </option>
        </select>
      </div>
    </div>

    <div v-if="feedingLoading" class="feeding-skeleton">
      <div></div>
      <div></div>
      <div></div>
    </div>

    <TransitionGroup v-else name="plan-list" tag="div">
      <div v-for="plan in feedingPlans" :key="planKey(plan)" class="feeding-plan">
        <h2>{{ planTitle(plan) }}</h2>
        <div class="plan-details">
          <div class="plan-item metric-card">
            <h3>每日能量需求</h3>
            <p class="metric-value">
              {{ formatNumber(plan.dailyEnergy) }} <span>kcal</span>
            </p>
          </div>
          <div class="plan-item ratio-card">
            <h3>食物配比</h3>
            <div class="ratio-bars">
              <div
                v-for="item in ratioItems(plan)"
                :key="item.name"
                class="ratio-row"
                :style="{ '--percent': `${item.value}%`, '--bar-color': item.color }"
              >
                <div class="ratio-row-head">
                  <span><i></i>{{ item.name }}</span>
                  <strong>{{ item.value }}%</strong>
                </div>
                <div class="ratio-track">
                  <div class="ratio-fill"></div>
                </div>
              </div>
            </div>
          </div>
          <div class="plan-item metric-card">
            <h3>喂食频次</h3>
            <p class="metric-value">
              {{ plan.frequency || '-' }} <span>次/天</span>
            </p>
          </div>
          <div class="plan-item metric-card">
            <h3>罐头/湿粮单次量</h3>
            <p class="metric-value">
              {{ formatNumber(plan.cannedPortionSize || plan.portionSize) }} <span>g/次</span>
            </p>
          </div>
          <div class="plan-item metric-card">
            <h3>冻干单次量</h3>
            <p class="metric-value">
              {{ formatNumber(plan.freezeDriedPortionSize) }} <span>g/次</span>
            </p>
          </div>
        </div>

        <div class="feeding-timeline">
          <h3>今日推荐时间</h3>
          <div class="timeline-slots">
            <div v-for="time in feedingTimes(plan.frequency)" :key="time" class="timeline-slot">
              <span>{{ time }}</span>
              <button
                type="button"
                :class="{ checked: isFeedingChecked(plan, time) }"
                @click="toggleFeedingCheck(plan, time)"
              >
                {{ isFeedingChecked(plan, time) ? '已打卡' : '今日打卡' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </TransitionGroup>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import axios from 'axios'

const pets = ref([])
const selectedPetId = ref('')
const feedingPlans = ref([])
const feedingLoading = ref(false)
const checkedFeedings = ref({})
const todayKey = formatDateKey(new Date())

function formatDateKey(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const selectedPetName = computed(() => {
  const pet = pets.value.find(p => p.id == selectedPetId.value)
  return pet ? pet.name : ''
})

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
  return labels[petType] || '未分类'
}

const formatNumber = (value) => {
  const numberValue = Number(value)
  return Number.isFinite(numberValue) ? numberValue.toFixed(1) : '0.0'
}

const normalizePercent = (value) => {
  const numberValue = Number(value)
  if (!Number.isFinite(numberValue)) return 0
  return Math.min(100, Math.max(0, Math.round(numberValue)))
}

const ratioItems = (plan) => [
  { name: '蛋白质', value: normalizePercent(plan.protein), color: '#3b82f6' },
  { name: '脂肪', value: normalizePercent(plan.fat), color: '#f59e0b' },
  { name: '碳水', value: normalizePercent(plan.carbs), color: '#10b981' }
]

const planKey = (plan) => plan.id || plan.pet?.id || `${planTitle(plan)}-${plan.frequency}`

const planPetId = (plan) => plan.pet?.id || pets.value.find(pet => planTitle(plan).startsWith(pet.name))?.id || selectedPetId.value || 'unknown'

const checkinStorageKey = () => `pet-feeding-checkins-${todayKey}`

const loadCheckins = () => {
  try {
    checkedFeedings.value = JSON.parse(localStorage.getItem(checkinStorageKey()) || '{}')
  } catch (error) {
    checkedFeedings.value = {}
  }
}

const persistCheckins = () => {
  localStorage.setItem(checkinStorageKey(), JSON.stringify(checkedFeedings.value))
}

const feedingTimes = (frequency) => {
  const count = Number(frequency)
  if (!Number.isFinite(count) || count <= 0) return ['08:00', '19:00']
  if (count <= 1) return ['18:00']
  if (count === 2) return ['08:00', '19:00']
  if (count === 3) return ['08:00', '13:00', '19:00']
  return ['07:30', '12:00', '17:30', '21:00']
}

const feedingCheckKey = (plan, time) => `${planPetId(plan)}-${time}`

const isFeedingChecked = (plan, time) => Boolean(checkedFeedings.value[feedingCheckKey(plan, time)])

const toggleFeedingCheck = (plan, time) => {
  const key = feedingCheckKey(plan, time)
  checkedFeedings.value = {
    ...checkedFeedings.value,
    [key]: !checkedFeedings.value[key]
  }
  persistCheckins()
}

const planTitle = (plan) => {
  const pet = plan.pet || pets.value.find(item => item.id == selectedPetId.value)
  if (!pet) {
    return `${selectedPetName.value}的喂养方案`
  }
  return `${pet.name}的喂养方案（${petTypeText(pet.petType)} / ${pet.breed}）`
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

const generateFeedingPlan = async () => {
  if (!selectedPetId.value) {
    feedingPlans.value = []
    return
  }
  
  try {
    feedingLoading.value = true
    const response = await axios.get(`/api/feeding-plan/${selectedPetId.value}`)
    feedingPlans.value = [response.data]
  } catch (error) {
    console.error('生成喂养方案失败', error)
  } finally {
    feedingLoading.value = false
  }
}

const generateAllFeedingPlans = async () => {
  try {
    feedingLoading.value = true
    selectedPetId.value = ''
    const responses = await Promise.all(
      pets.value.map(pet => axios.get(`/api/feeding-plan/${pet.id}`))
    )
    feedingPlans.value = responses.map(response => response.data)
  } catch (error) {
    console.error('生成所有喂养方案失败', error)
  } finally {
    feedingLoading.value = false
  }
}

onMounted(() => {
  loadCheckins()
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

.feeding-controls {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 30px;
  flex-wrap: wrap;
}

.generate-all-btn {
  padding: 9px 16px;
  border: none;
  border-radius: 4px;
  background-color: #4CAF50;
  color: white;
  cursor: pointer;
  font-size: 15px;
}

.generate-all-btn:disabled {
  background-color: #bdbdbd;
  cursor: not-allowed;
}

.feeding-skeleton {
  display: grid;
  gap: 16px;
}

.feeding-skeleton div {
  height: 116px;
  border-radius: 8px;
  background: linear-gradient(90deg, #edf2ef 25%, #f7faf8 37%, #edf2ef 63%);
  background-size: 400% 100%;
  animation: skeleton-loading 1.2s ease-in-out infinite;
}

@keyframes skeleton-loading {
  0% {
    background-position: 100% 50%;
  }

  100% {
    background-position: 0 50%;
  }
}

.pet-selector {
  display: flex;
  align-items: center;
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
  padding: 22px;
  border-radius: 8px;
  border: 1px solid #e0e7e3;
  margin-bottom: 20px;
  transition: all 0.3s ease;
}

.feeding-plan h2 {
  color: #1f2d26;
  margin-bottom: 20px;
}

.plan-details {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(190px, 1fr));
  gap: 14px;
}

.plan-item {
  background-color: #f7faf8;
  padding: 16px;
  border-radius: 8px;
}

.plan-item h3 {
  color: #52665a;
  margin-bottom: 12px;
  font-size: 14px;
}

.metric-value {
  color: #1f2d26;
  font-size: 28px;
  font-weight: 800;
  line-height: 1.1;
}

.metric-value span {
  color: #6f7f75;
  font-size: 13px;
  font-weight: 700;
  margin-left: 4px;
}

.ratio-card {
  grid-column: span 2;
}

.ratio-bars {
  display: grid;
  gap: 12px;
}

.ratio-row-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 7px;
  color: #35453b;
  font-size: 14px;
}

.ratio-row-head span {
  display: inline-flex;
  align-items: center;
  gap: 7px;
}

.ratio-row-head i {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: var(--bar-color);
}

.ratio-row-head strong {
  color: #1f2d26;
}

.ratio-track {
  height: 8px;
  overflow: hidden;
  border-radius: 999px;
  background-color: #e7eee9;
}

.ratio-fill {
  width: var(--percent);
  height: 100%;
  border-radius: inherit;
  background-color: var(--bar-color);
}

.feeding-timeline {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #e3e9e6;
}

.feeding-timeline h3 {
  color: #52665a;
  margin-bottom: 12px;
  font-size: 14px;
}

.timeline-slots {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 10px;
}

.timeline-slot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 10px 12px;
  border: 1px solid #e0e7e3;
  border-radius: 8px;
  background-color: #fbfcfb;
}

.timeline-slot span {
  color: #1f2d26;
  font-size: 18px;
  font-weight: 800;
}

.timeline-slot button {
  padding: 7px 10px;
  border: none;
  border-radius: 6px;
  background-color: #e8f2ff;
  color: #2563a6;
  cursor: pointer;
  font-size: 13px;
  font-weight: 700;
}

.timeline-slot button.checked {
  background-color: #edf7ef;
  color: #2e7d52;
}

.plan-list-enter-active,
.plan-list-leave-active {
  transition: all 0.3s ease;
}

.plan-list-enter-from,
.plan-list-leave-to {
  opacity: 0;
  transform: translateY(8px);
}

@media (max-width: 720px) {
  .ratio-card {
    grid-column: span 1;
  }
}
</style>
