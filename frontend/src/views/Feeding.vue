<template>
  <div class="feeding">
    <section class="feeding-hero">
      <div class="feeding-title">
        <span class="algorithm-dot"></span>
        <div>
          <p>AI Feeding Plan</p>
          <h1>智能喂养方案</h1>
        </div>
      </div>
      <div class="feeding-controls">
        <div class="pet-selector">
          <PetAvatar :pet="selectedPetForHeader" :type="selectedPetForHeader.petType || 'cat'" size="sm" circle />
          <select id="petId" v-model="selectedPetId" aria-label="选择宠物" @change="generateFeedingPlan">
            <option value="">请选择宠物</option>
            <option v-for="pet in pets" :key="pet.id" :value="pet.id">
              {{ pet.name }}（{{ petTypeText(pet.petType) }} / {{ pet.breed }}）
            </option>
          </select>
          <svg viewBox="0 0 24 24" aria-hidden="true">
            <path d="M6 9l6 6 6-6" />
          </svg>
        </div>
        <button class="generate-all-btn" :disabled="pets.length === 0 || feedingLoading" @click="generateAllFeedingPlans">
          生成所有宠物喂养方案
        </button>
      </div>
    </section>

    <div v-if="feedingLoading" class="feeding-skeleton">
      <div></div>
      <div></div>
      <div></div>
    </div>

    <TransitionGroup v-else name="plan-list" tag="div">
      <div
        v-for="plan in visiblePlans"
        :key="planKey(plan)"
        class="feeding-plan"
        :class="{ placeholder: isPlaceholderPlan(plan) }"
      >
        <h2>{{ planTitle(plan) }}</h2>
        <div class="plan-details">
          <div class="plan-main-card">
            <div class="energy-block">
              <h3>每日能量需求</h3>
              <p class="energy-value">
                {{ formatMetric(plan.dailyEnergy) }} <span>kcal</span>
              </p>
            </div>
            <div class="ratio-block">
              <h3>食物配比</h3>
              <div
                class="ratio-segment"
                :style="ratioSegmentStyle(plan)"
                aria-label="蛋白质、脂肪和碳水配比"
              >
                <span
                  v-for="item in ratioItems(plan)"
                  :key="item.name"
                  :class="item.key"
                  :style="{ width: `${item.value}%` }"
                ></span>
              </div>
              <div class="ratio-legend">
                <span v-for="item in ratioItems(plan)" :key="item.name">
                  <i :style="{ backgroundColor: item.color }"></i>
                  {{ item.name }} {{ isPlaceholderPlan(plan) ? '—' : `${item.value}%` }}
                </span>
              </div>
            </div>
          </div>

          <div class="plan-item frequency-card">
            <h3>喂食频次</h3>
            <p class="metric-value">
              {{ formatPlainMetric(plan.frequency) }} <span>次/天</span>
            </p>
          </div>

          <div class="portion-row">
            <div class="plan-item metric-card">
              <h3>罐头/湿粮单次量</h3>
              <p class="metric-value">
                {{ formatMetric(plan.cannedPortionSize || plan.portionSize) }} <span>g/次</span>
              </p>
            </div>
            <div class="plan-item metric-card">
              <h3>冻干单次量</h3>
              <p class="metric-value">
                {{ formatMetric(plan.freezeDriedPortionSize) }} <span>g/次</span>
              </p>
            </div>
            <div class="plan-item metric-card">
              <h3>推荐饮水量</h3>
              <p class="metric-value">
                {{ recommendedWater(plan) }} <span>ml/天</span>
              </p>
            </div>
          </div>
        </div>

        <div class="feeding-timeline">
          <h3>今日推荐时间</h3>
          <div class="timeline-slots">
            <div v-for="time in feedingTimes(plan.frequency)" :key="time" class="timeline-slot">
              <div class="time-label">
                <i aria-hidden="true"></i>
                <span>{{ time }}</span>
              </div>
              <button
                type="button"
                :disabled="isPlaceholderPlan(plan)"
                :class="{ checked: isFeedingChecked(plan, time) }"
                @click="toggleFeedingCheck(plan, time)"
              >
                {{ isPlaceholderPlan(plan) ? '待生成' : isFeedingChecked(plan, time) ? '已打卡' : '今日打卡' }}
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
import { useRoute } from 'vue-router'
import axios from 'axios'
import PetAvatar from '../components/PetAvatar.vue'

const route = useRoute()
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

const selectedPetForHeader = computed(() => {
  return selectedPetId.value ? pets.value.find(p => p.id == selectedPetId.value) || {} : {}
})

const placeholderPlan = {
  placeholder: true,
  dailyEnergy: null,
  protein: null,
  fat: null,
  carbs: null,
  frequency: null,
  portionSize: null,
  cannedPortionSize: null,
  freezeDriedPortionSize: null
}

const visiblePlans = computed(() => feedingPlans.value.length > 0 ? feedingPlans.value : [placeholderPlan])

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

const formatMetric = (value) => {
  const numberValue = Number(value)
  return Number.isFinite(numberValue) ? numberValue.toFixed(1) : '—'
}

const formatPlainMetric = (value) => {
  const numberValue = Number(value)
  return Number.isFinite(numberValue) ? String(numberValue) : '—'
}

const normalizePercent = (value) => {
  const numberValue = Number(value)
  if (!Number.isFinite(numberValue)) return 0
  return Math.min(100, Math.max(0, Math.round(numberValue)))
}

const ratioItems = (plan) => [
  { key: 'protein', name: '蛋白质', value: normalizePercent(plan.protein), color: '#3b82f6' },
  { key: 'fat', name: '脂肪', value: normalizePercent(plan.fat), color: '#f59e0b' },
  { key: 'carbs', name: '碳水', value: normalizePercent(plan.carbs), color: '#10b981' }
]

const ratioSegmentStyle = (plan) => {
  if (isPlaceholderPlan(plan)) {
    return {
      background: 'linear-gradient(90deg, #e5ece8 0 100%)'
    }
  }
  const [protein, fat, carbs] = ratioItems(plan)
  const proteinEnd = protein.value
  const fatEnd = protein.value + fat.value
  return {
    background: `linear-gradient(90deg, ${protein.color} 0 ${proteinEnd}%, ${fat.color} ${proteinEnd}% ${fatEnd}%, ${carbs.color} ${fatEnd}% 100%)`
  }
}

const recommendedWater = (plan) => {
  if (isPlaceholderPlan(plan)) return '—'
  const pet = plan.pet || pets.value.find(item => item.id == selectedPetId.value)
  const weight = Number(pet?.weight)
  if (!Number.isFinite(weight) || weight <= 0) return '0'
  const petType = pet?.petType
  const mlPerKg = petType === 'cat' ? 50 : 60
  return Math.round(weight * mlPerKg)
}

const planKey = (plan) => plan.id || plan.pet?.id || `${planTitle(plan)}-${plan.frequency}`

const isPlaceholderPlan = (plan) => Boolean(plan?.placeholder)

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
  if (!Number.isFinite(count)) return ['—', '—']
  if (!Number.isFinite(count) || count <= 0) return ['08:00', '19:00']
  if (count <= 1) return ['18:00']
  if (count === 2) return ['08:00', '19:00']
  if (count === 3) return ['08:00', '13:00', '19:00']
  return ['07:30', '12:00', '17:30', '21:00']
}

const feedingCheckKey = (plan, time) => `${planPetId(plan)}-${time}`

const isFeedingChecked = (plan, time) => Boolean(checkedFeedings.value[feedingCheckKey(plan, time)])

const toggleFeedingCheck = (plan, time) => {
  if (isPlaceholderPlan(plan)) return
  const key = feedingCheckKey(plan, time)
  checkedFeedings.value = {
    ...checkedFeedings.value,
    [key]: !checkedFeedings.value[key]
  }
  persistCheckins()
}

const planTitle = (plan) => {
  if (isPlaceholderPlan(plan)) {
    return '选择宠物后生成专属喂养方案'
  }
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
    return response.data
  } catch (error) {
    console.error('获取宠物列表失败', error)
    return []
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

onMounted(async () => {
  loadCheckins()
  const petList = await fetchPets()
  const queryPetId = route.query.petId ? String(route.query.petId) : ''
  if (queryPetId && petList.some(pet => String(pet.id) === queryPetId)) {
    selectedPetId.value = queryPetId
    generateFeedingPlan()
  }
})
</script>

<style scoped>
.feeding {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

h1 {
  color: #1f2d26;
  font-size: 28px;
  line-height: 1.2;
}

.feeding-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  flex-wrap: wrap;
  margin-bottom: 28px;
  padding: 18px;
  border: 1px solid #e0e7e3;
  border-radius: 12px;
  background: linear-gradient(135deg, #ffffff 0%, #f7faf8 100%);
}

.feeding-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.feeding-title p {
  color: #60756a;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.08em;
  margin-bottom: 4px;
}

.algorithm-dot {
  width: 12px;
  height: 42px;
  border-radius: 999px;
  background: linear-gradient(180deg, #10b981, #3b82f6);
}

.feeding-controls {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  flex-wrap: wrap;
}

.generate-all-btn {
  min-height: 44px;
  padding: 10px 16px;
  border: none;
  border-radius: 10px;
  background-color: #1f2d26;
  color: white;
  cursor: pointer;
  font-size: 14px;
  font-weight: 800;
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
  position: relative;
  display: flex;
  align-items: center;
  gap: 10px;
  min-height: 44px;
  padding: 5px 40px 5px 8px;
  border: 1px solid #dce5df;
  border-radius: 12px;
  background-color: rgba(255, 255, 255, 0.88);
}

.pet-selector select {
  min-width: 220px;
  padding: 0;
  border: none;
  outline: none;
  appearance: none;
  background-color: transparent;
  color: #25332b;
  font-size: 14px;
  font-weight: 800;
  cursor: pointer;
}

.pet-selector svg {
  position: absolute;
  right: 12px;
  width: 18px;
  height: 18px;
  fill: none;
  stroke: #66766c;
  stroke-width: 2.2;
  stroke-linecap: round;
  stroke-linejoin: round;
  pointer-events: none;
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

.feeding-plan.placeholder h2,
.feeding-plan.placeholder .energy-value,
.feeding-plan.placeholder .metric-value {
  color: #a8b5ad;
}

.feeding-plan.placeholder .ratio-legend {
  color: #8d9b92;
}

.feeding-plan.placeholder .timeline-slot button {
  background-color: #eef2ef;
  color: #8a9890;
  cursor: not-allowed;
}

.plan-details {
  display: grid;
  grid-template-columns: minmax(0, 2fr) minmax(220px, 1fr);
  gap: 14px;
}

.plan-main-card {
  display: grid;
  grid-template-columns: minmax(190px, 0.9fr) minmax(240px, 1.1fr);
  align-items: center;
  gap: 22px;
  min-height: 168px;
  padding: 20px;
  border-radius: 8px;
  background-color: #f7faf8;
}

.plan-item {
  background-color: #f7faf8;
  padding: 16px;
  border-radius: 8px;
}

.plan-item h3,
.plan-main-card h3 {
  color: #52665a;
  margin-bottom: 12px;
  font-size: 14px;
}

.energy-value,
.metric-value {
  color: #1f2d26;
  font-size: 28px;
  font-weight: 800;
  line-height: 1.1;
}

.energy-value {
  font-size: 42px;
}

.energy-value span,
.metric-value span {
  color: #6f7f75;
  font-size: 13px;
  font-weight: 700;
  margin-left: 4px;
}

.frequency-card {
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-height: 168px;
}

.portion-row {
  grid-column: 1 / -1;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.ratio-block {
  min-width: 0;
}

.ratio-segment {
  display: flex;
  width: 100%;
  height: 18px;
  overflow: hidden;
  border-radius: 999px;
  box-shadow: inset 0 0 0 1px rgba(31, 45, 38, 0.06);
}

.ratio-segment span {
  height: 100%;
}

.ratio-legend {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px 14px;
  margin-top: 12px;
  color: #35453b;
  font-size: 13px;
}

.ratio-legend span {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.ratio-legend i {
  width: 8px;
  height: 8px;
  border-radius: 50%;
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
  gap: 22px;
  position: relative;
}

.timeline-slots::before {
  position: absolute;
  left: 18px;
  right: 18px;
  top: 50%;
  z-index: 0;
  content: '';
  border-top: 1px dashed #cfdad4;
}

.timeline-slot {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 10px 12px;
  border: 1px solid #e0e7e3;
  border-radius: 8px;
  background-color: #fbfcfb;
}

.timeline-slot:not(:last-child)::after {
  position: absolute;
  right: -17px;
  top: 50%;
  content: '→';
  color: #9aac9f;
  font-size: 16px;
  font-weight: 700;
  transform: translateY(-50%);
}

.time-label {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.time-label i {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background-color: #10b981;
  box-shadow: 0 0 0 4px rgba(16, 185, 129, 0.12);
}

.time-label span {
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
  background-color: #ecfdf5;
  color: #047857;
  font-weight: 800;
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

@media (max-width: 900px) {
  .feeding-hero {
    align-items: stretch;
    flex-direction: column;
  }

  .feeding-controls {
    justify-content: stretch;
  }

  .pet-selector,
  .generate-all-btn {
    width: 100%;
  }

  .pet-selector select {
    width: 100%;
    min-width: 0;
  }

  .plan-details,
  .plan-main-card,
  .portion-row {
    grid-template-columns: 1fr;
  }

  .frequency-card {
    min-height: auto;
  }
}

@media (max-width: 720px) {
  .timeline-slots {
    grid-template-columns: 1fr;
    gap: 10px;
  }

  .timeline-slots::before,
  .timeline-slot::after {
    display: none;
  }
}
</style>
