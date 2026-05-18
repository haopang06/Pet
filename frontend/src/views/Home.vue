<template>
  <div class="home">
    <section class="dashboard-head">
      <div>
        <p class="eyebrow">今日概览</p>
        <h1>宠物智能喂养控制台</h1>
      </div>
      <div class="head-actions">
        <select v-model="selectedPetId" :disabled="pets.length === 0" aria-label="选择宠物">
          <option v-for="pet in pets" :key="pet.id" :value="pet.id">
            {{ pet.name }} / {{ petTypeText(resolvePetType(pet)) }}
          </option>
        </select>
        <router-link class="primary-action" to="/pets">管理宠物</router-link>
      </div>
    </section>

    <div v-if="!isLoggedIn" class="notice">
      <span>登录后可以查看自己的宠物、喂养方案和健康预警。</span>
      <router-link to="/login">去登录</router-link>
    </div>

    <div v-else-if="pets.length === 0" class="empty-state">
      <div class="empty-visual" aria-hidden="true">
        <span></span>
      </div>
      <h2>还没有宠物资料</h2>
      <p>先添加宠物信息，系统会根据类型、品种、体重和活动量生成喂养建议。</p>
      <router-link to="/pets">添加宠物</router-link>
    </div>

    <section v-else class="dashboard-grid">
      <aside class="pet-summary">
        <div class="pet-main">
          <PetAvatar :pet="selectedPet" :type="resolvePetType(selectedPet)" size="lg" />
          <div>
            <h2>{{ selectedPet.name }}</h2>
            <p>{{ petTypeText(resolvePetType(selectedPet)) }} / {{ selectedPet.breed || '未填写品种' }}</p>
          </div>
        </div>

        <div class="status-tags">
          <span>{{ selectedPet.age ?? '-' }} 岁</span>
          <span>{{ formatWeight(selectedPet.weight) }}</span>
          <span class="activity-badge" :class="activityLevelClass(selectedPet.activityLevel)">
            活动量 {{ activityLevelText(selectedPet.activityLevel) }}
          </span>
        </div>

        <div class="pet-list">
          <button
            v-for="pet in pets"
            :key="pet.id"
            type="button"
            :class="{ active: String(pet.id) === String(selectedPetId) }"
            @click="selectedPetId = String(pet.id)"
          >
            <PetAvatar :pet="pet" :type="resolvePetType(pet)" size="sm" />
            <span>{{ pet.name }}</span>
            <small>{{ pet.breed }}</small>
          </button>
        </div>
      </aside>

      <div class="main-panels">
        <section class="panel feeding-panel">
          <div class="panel-title">
            <div>
              <p class="section-label">今日喂养</p>
              <h2>{{ feedingHeadline }}</h2>
            </div>
            <router-link to="/feeding">查看方案</router-link>
          </div>

          <div class="feeding-metrics">
            <div>
              <span>每日能量</span>
              <strong>{{ formatNumber(feedingPlan?.dailyEnergy) }} kcal</strong>
            </div>
            <div>
              <span>建议频次</span>
              <strong>{{ feedingPlan?.frequency || '-' }} 次/天</strong>
            </div>
            <div>
              <span>湿粮单次</span>
              <strong>{{ formatNumber(feedingPlan?.cannedPortionSize || feedingPlan?.portionSize) }} g</strong>
            </div>
            <div>
              <span>冻干单次</span>
              <strong>{{ formatNumber(feedingPlan?.freezeDriedPortionSize) }} g</strong>
            </div>
          </div>

          <div class="schedule-row">
            <div class="checkin-summary">
              <span class="dot done"></span>
              <span>{{ todayCheckinText }}</span>
              <router-link
                v-if="showFeedingCheckinLink"
                :to="feedingCheckinLink"
              >
                去打卡
              </router-link>
            </div>
            <div>
              <span class="dot pending"></span>
              {{ pendingFeedText }}
            </div>
          </div>
        </section>

        <section class="panel health-panel">
          <div class="panel-title">
            <div>
              <p class="section-label">健康简报</p>
              <h2>{{ healthHeadline }}</h2>
            </div>
            <router-link to="/health">查看健康</router-link>
          </div>

          <div class="health-body">
            <div class="health-latest">
              <span>最近记录</span>
              <strong>{{ latestHealthText }}</strong>
              <small>{{ latestHealthDetail }}</small>
            </div>
            <div class="health-latest" :class="{ warning: weeklyAbnormalCount >= 3 }">
              <span>近 7 天异常</span>
              <strong>{{ weeklyAbnormalCount }} 天</strong>
              <small>{{ weeklyAbnormalCount >= 3 ? '请多加留意' : '状态相对稳定' }}</small>
            </div>
          </div>

          <div
            class="alert-strip"
            :class="{
              muted: latestAlerts.length === 0,
              handled: latestAlerts.length > 0 && activeAlertCount === 0
            }"
          >
            <strong>
              {{ alertStripTitle }}
              <em v-if="latestAlerts.length">{{ latestAlertStatusText }}</em>
            </strong>
            <span>{{ latestAlertText }}</span>
          </div>
        </section>

        <section class="panel note-panel">
          <div class="panel-title">
            <div>
              <p class="section-label">宠物备忘录</p>
              <h2>备注</h2>
            </div>
            <button type="button" :disabled="noteSaving" @click="savePetNote">
              {{ noteSaving ? '保存中' : '保存备注' }}
            </button>
          </div>
          <textarea
            v-model.trim="noteDraft"
            rows="4"
            maxlength="1000"
            placeholder="记录事项，例如疫苗、过敏、偏好、医生建议等"
          ></textarea>
          <div class="note-image-actions">
            <input id="noteImageInput" ref="noteImageInput" class="file-input" type="file" accept="image/*" multiple @change="handleNoteImagesChange">
            <label class="image-add-btn" for="noteImageInput">添加图片</label>
          </div>
          <div v-if="noteImages.length" class="note-images">
            <div v-for="(image, index) in noteImages" :key="`${index}-${image.slice(0, 24)}`" class="note-image">
              <img :src="image" alt="备注图片">
              <button type="button" @click="removeNoteImage(index)">删除</button>
            </div>
          </div>
          <p class="note-status">{{ noteStatus }}</p>
        </section>
      </div>

      <section class="quick-panel">
        <h2>常用操作</h2>
        <router-link to="/pets">
          <span>宠物信息</span>
          <strong>{{ pets.length }} 只</strong>
        </router-link>
        <router-link to="/feeding">
          <span>智能喂养</span>
          <strong>{{ feedingPlan ? '已生成' : '待生成' }}</strong>
        </router-link>
        <router-link to="/health">
          <span>健康监测</span>
          <strong>{{ activeAlertCount }} 条未处理</strong>
        </router-link>
      </section>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import axios from 'axios'
import PetAvatar from '../components/PetAvatar.vue'
import { fileToCompressedDataUrl } from '../utils/image'

const pets = ref([])
const selectedPetId = ref('')
const feedingPlan = ref(null)
const healthRecords = ref([])
const latestAlerts = ref([])
const noteDraft = ref('')
const noteImages = ref([])
const noteSaving = ref(false)
const noteStatus = ref('')
const noteImageInput = ref(null)
const checkedFeedings = ref({})
const todayKey = formatDate(new Date())

const breedTypeMap = {
  美短: 'cat',
  英短: 'cat',
  金渐层: 'cat',
  银渐层: 'cat',
  布偶: 'cat',
  暹罗: 'cat',
  缅因: 'cat',
  加菲: 'cat',
  波斯: 'cat',
  狸花: 'cat',
  中华田园猫: 'cat',
  金毛: 'dog',
  哈士奇: 'dog',
  萨摩耶: 'dog',
  拉布拉多: 'dog',
  边牧: 'dog',
  德牧: 'dog',
  阿拉斯加: 'dog',
  柯基: 'dog',
  泰迪: 'dog',
  比熊: 'dog',
  中华田园犬: 'dog'
}

const isLoggedIn = computed(() => Boolean(localStorage.getItem('token') && localStorage.getItem('userId')))

const selectedPet = computed(() => {
  return pets.value.find(pet => String(pet.id) === String(selectedPetId.value)) || pets.value[0] || {}
})

const feedingHeadline = computed(() => {
  if (!feedingPlan.value) return '等待生成喂养方案'
  return `${selectedPet.value.name} 今日建议 ${feedingPlan.value.frequency || '-'} 次喂食`
})

const pendingFeedText = computed(() => {
  const frequency = Number(feedingPlan.value?.frequency)
  if (!Number.isFinite(frequency) || frequency <= 0) return '暂无待喂食提醒'
  if (frequency <= 2) return '建议安排早晚两次喂食'
  return '建议分早、中、晚多次少量喂食'
})

const todayFeedingTimes = computed(() => {
  if (!feedingPlan.value) return []
  return feedingTimes(feedingPlan.value.frequency)
})

const checkedFeedingCount = computed(() => (
  todayFeedingTimes.value.filter(time => isFeedingChecked(time)).length
))

const todayCheckinText = computed(() => {
  const total = todayFeedingTimes.value.length
  if (!feedingPlan.value || total === 0) return '今日已打卡（0/0）'
  return `今日已打卡（${checkedFeedingCount.value}/${total}）`
})

const showFeedingCheckinLink = computed(() => (
  Boolean(feedingPlan.value)
    && todayFeedingTimes.value.length > 0
    && checkedFeedingCount.value < todayFeedingTimes.value.length
))

const feedingCheckinLink = computed(() => ({
  path: '/feeding',
  query: {
    petId: selectedPetId.value
  }
}))

const sortedHealthRecords = computed(() => {
  return [...healthRecords.value].sort((a, b) => new Date(b.date) - new Date(a.date))
})

const latestHealthRecord = computed(() => sortedHealthRecords.value[0] || null)

const weeklyAbnormalCount = computed(() => {
  const weekKeys = getRecentWeekDateKeys()
  return weekKeys.filter(date => {
    return healthRecords.value.some(record => {
      const recordDate = String(record.date).slice(0, 10)
      return recordDate === date && isAbnormalRecord(record)
    })
  }).length
})

const healthHeadline = computed(() => {
  if (weeklyAbnormalCount.value >= 3) return '最近一周状态不佳'
  if (latestAlerts.value.length > 0) return '存在健康预警'
  return '暂无明显异常'
})

const latestHealthText = computed(() => {
  if (!latestHealthRecord.value) return '暂无记录'
  return `${formatWeight(latestHealthRecord.value.weight)} / ${mentalStateText(latestHealthRecord.value.mentalState)}`
})

const latestHealthDetail = computed(() => {
  if (!latestHealthRecord.value) return '可到健康监测页补充今天的数据'
  return `${String(latestHealthRecord.value.date).slice(0, 10)}，排便${defecationText(latestHealthRecord.value.defecation)}`
})

const latestAlertText = computed(() => {
  if (latestAlerts.value.length === 0) return '健康监测页记录异常后会在这里同步显示'
  const alert = latestAlerts.value[0]
  return `${alert.message || '请关注健康变化'}${alert.date ? `（${String(alert.date).slice(0, 10)}）` : ''}`
})

const activeAlertCount = computed(() => latestAlerts.value.filter(alert => !isAlertHandled(alert)).length)

const latestAlertStatusText = computed(() => {
  if (latestAlerts.value.length === 0) return ''
  return isAlertHandled(latestAlerts.value[0]) ? '已处理/已好转' : '未处理'
})

const alertStripTitle = computed(() => {
  if (latestAlerts.value.length === 0) return '暂无健康预警'
  return activeAlertCount.value > 0 ? '最新健康预警' : '健康预警已处理'
})

const getCurrentUserId = () => {
  const userId = Number(localStorage.getItem('userId'))
  return Number.isFinite(userId) && userId > 0 ? userId : null
}

const getUserParams = () => {
  const userId = getCurrentUserId()
  return userId ? { userId } : {}
}

const resolvePetType = (pet) => pet?.petType || breedTypeMap[pet?.breed] || 'cat'

const petTypeText = (petType) => {
  const labels = {
    cat: '猫',
    dog: '狗'
  }
  return labels[petType] || '未分类'
}

const activityLevelText = (activityLevel) => {
  const labels = {
    low: '低',
    medium: '中',
    high: '高'
  }
  return labels[activityLevel] || activityLevel || '-'
}

const activityLevelClass = (activityLevel) => {
  const classes = {
    low: 'activity-low',
    medium: 'activity-medium',
    high: 'activity-high'
  }
  return classes[activityLevel] || 'activity-medium'
}

const mentalStateText = (mentalState) => {
  const labels = {
    excellent: '优秀',
    good: '良好',
    normal: '正常',
    poor: '不佳'
  }
  return labels[mentalState] || mentalState || '无'
}

const defecationText = (defecation) => {
  const labels = {
    normal: '正常',
    soft: '偏软',
    hard: '偏硬',
    diarrhea: '腹泻'
  }
  return labels[defecation] || defecation || '无'
}

const formatNumber = (value) => {
  const numberValue = Number(value)
  return Number.isFinite(numberValue) ? numberValue.toFixed(1) : '0.0'
}

const formatWeight = (weight) => {
  const numberValue = Number(weight)
  return Number.isFinite(numberValue) ? `${numberValue.toFixed(1)} kg` : '-'
}

function formatDate(date) {
  const value = new Date(date)
  const year = value.getFullYear()
  const month = String(value.getMonth() + 1).padStart(2, '0')
  const day = String(value.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const getRecentWeekDateKeys = () => {
  const dates = []
  const baseDate = new Date()
  for (let offset = 6; offset >= 0; offset--) {
    const date = new Date(baseDate)
    date.setDate(baseDate.getDate() - offset)
    dates.push(formatDate(date))
  }
  return dates
}

const isAbnormalRecord = (record) => record.mentalState === 'poor' || record.defecation !== 'normal'

const checkinStorageKey = () => `pet-feeding-checkins-${todayKey}`

const loadCheckins = () => {
  try {
    checkedFeedings.value = JSON.parse(localStorage.getItem(checkinStorageKey()) || '{}')
  } catch (error) {
    checkedFeedings.value = {}
  }
}

const feedingTimes = (frequency) => {
  const count = Number(frequency)
  if (!Number.isFinite(count) || count <= 0) return []
  if (count <= 1) return ['18:00']
  if (count === 2) return ['08:00', '19:00']
  if (count === 3) return ['08:00', '13:00', '19:00']
  return ['07:30', '12:00', '17:30', '21:00']
}

const feedingCheckKey = (time) => `${selectedPetId.value}-${time}`

const isFeedingChecked = (time) => Boolean(checkedFeedings.value[feedingCheckKey(time)])

const normalizeHandled = (value) => value === true || value === 1 || value === '1' || value === 'true'

const getLocalHandledAlertIds = () => {
  try {
    const parsed = JSON.parse(localStorage.getItem('pet-handled-alert-ids') || '[]')
    return Array.isArray(parsed) ? parsed.map(String) : []
  } catch (error) {
    return []
  }
}

const isAlertHandled = (alert) => (
  normalizeHandled(alert?.handled) || getLocalHandledAlertIds().includes(String(alert?.id))
)

const fetchPets = async () => {
  if (!isLoggedIn.value) return

  try {
    const response = await axios.get('/api/pets', {
      params: getUserParams()
    })
    pets.value = response.data
    if (pets.value.length > 0 && !selectedPetId.value) {
      selectedPetId.value = String(pets.value[0].id)
    }
  } catch (error) {
    console.error('获取宠物列表失败', error)
  }
}

const fetchDashboardData = async () => {
  if (!selectedPetId.value) {
    feedingPlan.value = null
    healthRecords.value = []
    latestAlerts.value = []
    noteDraft.value = ''
    noteImages.value = []
    return
  }

  try {
    const [feedingResponse, recordsResponse, alertsResponse] = await Promise.all([
      axios.get(`/api/feeding-plan/${selectedPetId.value}`),
      axios.get(`/api/health-records/${selectedPetId.value}`),
      axios.get(`/api/health-alerts/${selectedPetId.value}`)
    ])
    feedingPlan.value = feedingResponse.data
    healthRecords.value = recordsResponse.data || []
    latestAlerts.value = normalizeAlerts(alertsResponse.data).sort(sortAlertsForHome).slice(0, 3)
  } catch (error) {
    console.error('获取首页概览失败', error)
    feedingPlan.value = null
    healthRecords.value = []
    latestAlerts.value = []
  }
}

const normalizeAlerts = (alerts) => {
  return (alerts || []).map(alert => ({
    ...alert,
    handled: isAlertHandled(alert)
  }))
}

const sortAlertsForHome = (a, b) => {
  if (isAlertHandled(a) !== isAlertHandled(b)) {
    return isAlertHandled(a) ? 1 : -1
  }
  return new Date(b.date) - new Date(a.date)
}

const resetNoteDraft = () => {
  noteDraft.value = selectedPet.value.note || ''
  noteImages.value = parseNoteImages(selectedPet.value.noteImages)
  noteStatus.value = ''
}

const parseNoteImages = (value) => {
  if (!value) return []
  try {
    const parsed = JSON.parse(value)
    return Array.isArray(parsed) ? parsed : []
  } catch (error) {
    return []
  }
}

const stringifyNoteImages = () => JSON.stringify(noteImages.value)

const savePetNote = async (successText = '备注已保存') => {
  if (!selectedPetId.value) return

  try {
    noteSaving.value = true
    const response = await axios.post(`/api/pets/${selectedPetId.value}/note`, {
      note: noteDraft.value,
      noteImages: stringifyNoteImages()
    })
    pets.value = pets.value.map(pet => String(pet.id) === String(selectedPetId.value) ? response.data : pet)
    noteDraft.value = response.data.note || ''
    noteImages.value = parseNoteImages(response.data.noteImages)
    noteStatus.value = successText
  } catch (error) {
    console.error('保存宠物备注失败', error)
    noteStatus.value = '保存失败，请稍后重试'
  } finally {
    noteSaving.value = false
  }
}

const handleNoteImagesChange = async (event) => {
  const selectedFiles = Array.from(event.target.files || [])
  const remainingSlots = Math.max(0, 6 - noteImages.value.length)
  if (remainingSlots === 0) {
    noteStatus.value = '最多添加 6 张图片'
    event.target.value = ''
    return
  }

  const files = selectedFiles.slice(0, remainingSlots)
  if (files.length === 0) {
    event.target.value = ''
    return
  }

  try {
    noteStatus.value = '图片处理中...'
    const images = await Promise.all(
      files.map(file => fileToCompressedDataUrl(file, 760, 0.7))
    )
    noteImages.value = [...noteImages.value, ...images]
    noteStatus.value = selectedFiles.length > files.length
      ? `已添加前 ${files.length} 张图片，正在保存...`
      : '图片已添加，正在保存...'
    await savePetNote('图片已保存')
  } catch (error) {
    noteStatus.value = error.message || '图片读取失败'
  } finally {
    event.target.value = ''
  }
}

const removeNoteImage = (index) => {
  noteImages.value = noteImages.value.filter((_, itemIndex) => itemIndex !== index)
  noteStatus.value = '图片已移除，点击保存备注后生效'
}

onMounted(() => {
  loadCheckins()
  fetchPets()
})

watch(selectedPetId, () => {
  loadCheckins()
  resetNoteDraft()
  fetchDashboardData()
})
</script>

<style scoped>
.home {
  max-width: 1280px;
  margin: 0 auto;
  padding: 18px 24px 32px;
}

.dashboard-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 18px;
}

.eyebrow,
.section-label {
  color: #60756a;
  font-size: 13px;
  font-weight: 700;
  margin-bottom: 6px;
}

h1 {
  color: #1f2d26;
  font-size: 28px;
  line-height: 1.25;
}

.head-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.head-actions select {
  min-width: 190px;
  padding: 9px 12px;
  border: 1px solid #d6ded9;
  border-radius: 6px;
  background-color: white;
  color: #26332c;
  font-size: 14px;
}

.primary-action,
.notice a,
.empty-state a,
.panel-title a,
.quick-panel a {
  text-decoration: none;
}

.primary-action {
  padding: 10px 14px;
  border-radius: 6px;
  background-color: #2e7d52;
  color: white;
  font-weight: 700;
}

.notice,
.empty-state {
  background-color: white;
  border: 1px solid #e0e7e3;
  border-radius: 8px;
}

.notice {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 16px 18px;
  color: #4b5d53;
}

.notice a {
  color: #2e7d52;
  font-weight: 700;
}

.empty-state {
  padding: 34px;
  color: #4b5d53;
  text-align: center;
}

.empty-state h2 {
  color: #1f2d26;
  margin-bottom: 10px;
}

.empty-state p {
  margin-bottom: 18px;
}

.empty-state a {
  display: inline-block;
  padding: 10px 14px;
  border-radius: 6px;
  background-color: #2e7d52;
  color: white;
  font-weight: 700;
}

.empty-visual {
  position: relative;
  width: 118px;
  height: 92px;
  margin: 0 auto 18px;
  border: 3px solid #8bb99e;
  border-radius: 46px 46px 30px 30px;
  background-color: #eef8f2;
}

.empty-visual::before,
.empty-visual::after {
  position: absolute;
  content: '';
  top: 28px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background-color: #2e7d52;
}

.empty-visual::before {
  left: 35px;
}

.empty-visual::after {
  right: 35px;
}

.empty-visual span {
  position: absolute;
  left: 50%;
  bottom: 27px;
  width: 28px;
  height: 11px;
  border-bottom: 3px solid #2e7d52;
  border-radius: 0 0 20px 20px;
  transform: translateX(-50%);
}

.dashboard-grid {
  display: grid;
  grid-template-columns: minmax(240px, 300px) minmax(0, 1fr) minmax(210px, 250px);
  gap: 18px;
  align-items: start;
}

.pet-summary,
.panel,
.quick-panel {
  background-color: white;
  border: 1px solid #e0e7e3;
  border-radius: 8px;
}

.pet-summary {
  padding: 18px;
}

.pet-main {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-bottom: 16px;
  border-bottom: 1px solid #edf1ef;
}

.pet-main h2 {
  color: #1f2d26;
  font-size: 22px;
  margin-bottom: 4px;
}

.pet-main p {
  color: #66766c;
  font-size: 14px;
}

.status-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin: 16px 0;
}

.status-tags span {
  padding: 6px 9px;
  border-radius: 6px;
  background-color: #f0f6f2;
  color: #2f5e43;
  font-size: 13px;
}

.status-tags .activity-badge {
  font-weight: 700;
}

.status-tags .activity-badge.activity-low {
  background-color: #e8f2ff;
  color: #2563a6;
}

.status-tags .activity-badge.activity-medium {
  background-color: #edf7ef;
  color: #2e7d52;
}

.status-tags .activity-badge.activity-high {
  background-color: #fff1dc;
  color: #a15c10;
}

.pet-list {
  display: grid;
  gap: 8px;
}

.pet-list button {
  display: grid;
  grid-template-columns: 38px minmax(0, 1fr);
  column-gap: 10px;
  row-gap: 2px;
  align-items: center;
  width: 100%;
  padding: 10px;
  border: 1px solid #e1e7e4;
  border-radius: 6px;
  background-color: #fbfcfb;
  text-align: left;
  cursor: pointer;
}

.pet-list button.active {
  border-color: #2e7d52;
  background-color: #f1f8f4;
}

.pet-list span,
.pet-list small {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pet-list span {
  color: #1f2d26;
  font-weight: 700;
}

.pet-list small {
  grid-column: 2;
  color: #738278;
}

.main-panels {
  display: grid;
  gap: 18px;
}

.panel,
.quick-panel {
  padding: 18px;
}

.panel-title {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.panel-title h2,
.quick-panel h2 {
  color: #1f2d26;
  font-size: 20px;
}

.panel-title a {
  color: #2e7d52;
  font-size: 14px;
  font-weight: 700;
  white-space: nowrap;
}

.feeding-metrics {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}

.feeding-metrics div,
.health-latest {
  padding: 12px;
  border-radius: 6px;
  background-color: #f7faf8;
}

.feeding-metrics span,
.health-latest span {
  display: block;
  color: #718178;
  font-size: 13px;
  margin-bottom: 7px;
}

.feeding-metrics strong,
.health-latest strong {
  display: block;
  color: #21332a;
  font-size: 19px;
}

.schedule-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  margin-top: 14px;
  color: #4d5f55;
  font-size: 14px;
}

.schedule-row div {
  padding: 10px 12px;
  border: 1px solid #e3e9e6;
  border-radius: 6px;
}

.schedule-row .checkin-summary {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.checkin-summary a {
  margin-left: auto;
  padding: 5px 9px;
  border-radius: 999px;
  background-color: #edf7ef;
  color: #2e7d52;
  font-size: 12px;
  font-weight: 800;
  text-decoration: none;
}

.dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 8px;
}

.dot.done {
  background-color: #2e7d52;
}

.dot.pending {
  background-color: #d9972b;
}

.health-body {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.health-latest small {
  display: block;
  color: #718178;
  margin-top: 6px;
}

.health-latest.warning {
  background-color: #fff1ed;
}

.health-latest.warning strong {
  color: #c24128;
}

.alert-strip {
  margin-top: 14px;
  padding: 12px 14px;
  border-left: 4px solid #c24128;
  border-radius: 6px;
  background-color: #fff1ed;
  color: #7d2c1f;
}

.alert-strip.muted {
  border-left-color: #7ba68c;
  background-color: #f1f8f4;
  color: #355a43;
}

.alert-strip.handled {
  border-left-color: #7ba68c;
  background-color: #f3f6f4;
  color: #4f6358;
}

.alert-strip strong {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 5px;
}

.alert-strip em {
  padding: 3px 8px;
  border-radius: 999px;
  background-color: rgba(194, 65, 40, 0.1);
  color: #c24128;
  font-size: 12px;
  font-style: normal;
  font-weight: 700;
}

.alert-strip.muted em,
.alert-strip.handled em {
  background-color: rgba(46, 125, 82, 0.12);
  color: #2e7d52;
}

.note-panel textarea {
  width: 100%;
  resize: vertical;
  min-height: 106px;
  padding: 12px;
  border: 1px solid #d6ded9;
  border-radius: 8px;
  color: #25332b;
  font-size: 14px;
  line-height: 1.6;
}

.note-image-actions {
  margin-top: 10px;
}

.file-input {
  position: absolute;
  width: 1px;
  height: 1px;
  overflow: hidden;
  clip: rect(0 0 0 0);
  white-space: nowrap;
}

.note-panel .image-add-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 8px 12px;
  border-radius: 6px;
  background-color: #edf7ef;
  color: #2e7d52;
  cursor: pointer;
  font-size: 14px;
  font-weight: 700;
}

.note-images {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(96px, 1fr));
  gap: 10px;
  margin-top: 10px;
}

.note-image {
  position: relative;
  overflow: hidden;
  aspect-ratio: 1;
  border: 1px solid #d6ded9;
  border-radius: 8px;
  background-color: #f7faf8;
}

.note-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.note-image button {
  position: absolute;
  right: 6px;
  bottom: 6px;
  padding: 4px 7px;
  border-radius: 5px;
  background-color: rgba(31, 45, 38, 0.78);
  color: white;
  font-size: 12px;
}

.note-panel textarea:focus {
  outline: none;
  border-color: #2e7d52;
  box-shadow: 0 0 0 3px rgba(46, 125, 82, 0.12);
}

.note-panel button {
  padding: 8px 12px;
  border: none;
  border-radius: 6px;
  background-color: #2e7d52;
  color: white;
  cursor: pointer;
  font-weight: 700;
  white-space: nowrap;
}

.note-panel button:disabled {
  background-color: #9aa8a0;
  cursor: not-allowed;
}

.note-status {
  min-height: 20px;
  margin-top: 8px;
  color: #60756a;
  font-size: 13px;
}

.quick-panel {
  display: grid;
  gap: 10px;
}

.quick-panel h2 {
  margin-bottom: 4px;
}

.quick-panel a {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 12px;
  border-radius: 6px;
  background-color: #f7faf8;
  color: #26332c;
}

.quick-panel a:hover {
  background-color: #edf5f0;
}

.quick-panel span {
  color: #56675d;
}

.quick-panel strong {
  color: #2e7d52;
}

@media (max-width: 1040px) {
  .dashboard-grid {
    grid-template-columns: 280px minmax(0, 1fr);
  }

  .quick-panel {
    grid-column: 1 / -1;
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .quick-panel h2 {
    grid-column: 1 / -1;
  }
}

@media (max-width: 780px) {
  .home {
    padding: 14px;
  }

  .dashboard-head,
  .head-actions,
  .notice {
    align-items: stretch;
    flex-direction: column;
  }

  .head-actions select {
    width: 100%;
  }

  .dashboard-grid,
  .feeding-metrics,
  .health-body,
  .schedule-row,
  .quick-panel {
    grid-template-columns: 1fr;
  }
}
</style>
