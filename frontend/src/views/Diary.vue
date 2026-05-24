<template>
  <div class="diary-page">
    <section class="page-hero">
      <div class="hero-title">
        <span class="hero-mark"></span>
        <div>
          <p>Pet Journal</p>
          <h1>宠物日记</h1>
        </div>
      </div>
      <div class="diary-selector">
        <PetAvatar :pet="selectedDiaryPet" :type="selectedDiaryPet.petType || 'cat'" size="sm" circle />
        <select id="petId" v-model="selectedPetId" aria-label="选择宠物" :disabled="pets.length === 0">
          <option value="">请选择宠物</option>
          <option v-for="pet in pets" :key="pet.id" :value="pet.id">
            {{ pet.name }}
          </option>
        </select>
        <svg viewBox="0 0 24 24" aria-hidden="true">
          <path d="M6 9l6 6 6-6" />
        </svg>
      </div>
    </section>

    <div v-if="!isLoggedIn" class="notice">
      <span>登录后才能保存宠物日记。</span>
      <router-link to="/login">去登录</router-link>
    </div>

    <div v-else-if="pets.length === 0" class="empty-state">
      <div class="empty-visual" aria-hidden="true">
        <span></span>
      </div>
      <h2>还没有宠物资料</h2>
      <p>先添加宠物信息，之后就可以按日期记录文字和照片日记。</p>
      <router-link to="/pets">添加宠物</router-link>
    </div>

    <div v-else class="diary-content" :class="{ placeholder: !hasSelectedPet }">
      <aside class="diary-calendar">
        <div class="calendar-head">
          <button type="button" class="calendar-nav" aria-label="上一个月" @click="shiftDiaryMonth(-1)">
            <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M15 18l-6-6 6-6" /></svg>
          </button>
          <strong>{{ diaryMonthLabel }}</strong>
          <button type="button" class="calendar-nav" aria-label="下一个月" @click="shiftDiaryMonth(1)">
            <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M9 6l6 6-6 6" /></svg>
          </button>
        </div>
        <div class="calendar-weekdays">
          <span v-for="weekday in diaryWeekdays" :key="weekday">{{ weekday }}</span>
        </div>
        <div class="calendar-grid">
          <button
            v-for="cell in diaryCalendarCells"
            :key="cell.key"
            type="button"
            class="calendar-cell"
            :class="cell.classes"
            :disabled="!cell.inMonth"
            @click="selectDiaryDate(cell.dateKey)"
          >
            <span v-if="cell.isFavorite" class="calendar-favorite" aria-hidden="true">★</span>
            <strong>{{ cell.dayLabel }}</strong>
            <small v-if="cell.hasEntry">有记录</small>
          </button>
        </div>
      </aside>

      <section class="diary-editor">
        <div class="diary-editor-head">
          <div>
            <p>记录日期</p>
            <h2>{{ selectedDiaryDate }}</h2>
          </div>
          <div class="diary-head-tools">
            <span class="entry-status" :class="{ saved: Boolean(selectedDiaryEntry) }">
              {{ selectedDiaryEntry ? '已保存记录' : '新建记录' }}
            </span>
            <button
              type="button"
              class="favorite-btn"
              :class="{ active: diaryFavorite }"
              :disabled="!hasSelectedPet || diarySaving"
              @click="toggleFavorite"
            >
              <span aria-hidden="true">⭐</span>
              {{ diaryFavorite ? '取消收藏' : '收藏' }}
            </button>
          </div>
        </div>

        <textarea
          v-model.trim="diaryContent"
          rows="7"
          maxlength="2000"
          :disabled="!hasSelectedPet"
          placeholder="记录今天的喂养、护理、用药、疫苗、情绪变化或外出情况"
        ></textarea>

        <div class="diary-image-actions">
          <input
            id="diaryImageInput"
            class="file-input"
            type="file"
            accept="image/*"
            multiple
            :disabled="!hasSelectedPet"
            @change="handleDiaryImagesChange"
          >
          <label class="image-add-btn" :class="{ disabled: !hasSelectedPet }" for="diaryImageInput">添加照片</label>
          <span>{{ diaryImages.length }}/6</span>
        </div>

        <div v-if="diaryImages.length" class="diary-images">
          <div v-for="(image, index) in diaryImages" :key="`${index}-${image.slice(0, 24)}`" class="diary-image">
            <img :src="image" alt="日记照片">
            <button type="button" :disabled="!hasSelectedPet" @click="removeDiaryImage(index)">删除</button>
          </div>
        </div>

        <div v-else class="diary-photo-empty">
          {{ hasSelectedPet ? '可以添加体检单、用药清单、食物照片等' : '选择宠物后可保存文字和照片日记' }}
        </div>

        <div class="diary-actions">
          <button type="button" :disabled="!hasSelectedPet || diarySaving" @click="saveDiaryEntry">
            {{ diarySaving ? '保存中' : '保存日记' }}
          </button>
          <p>{{ diaryStatus || diaryHint }}</p>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import axios from 'axios'
import PetAvatar from '../components/PetAvatar.vue'
import { fileToCompressedDataUrl } from '../utils/image'

const pets = ref([])
const selectedPetId = ref('')
const diaryEntries = ref([])
const diaryMonth = ref(startOfMonth(new Date()))
const selectedDiaryDate = ref(formatDate(new Date()))
const diaryContent = ref('')
const diaryImages = ref([])
const diaryFavorite = ref(false)
const diarySaving = ref(false)
const diaryStatus = ref('')
const today = formatDate(new Date())

const isLoggedIn = computed(() => Boolean(localStorage.getItem('token') && localStorage.getItem('userId')))

const selectedDiaryPet = computed(() => {
  return selectedPetId.value
    ? pets.value.find(pet => String(pet.id) === String(selectedPetId.value)) || {}
    : {}
})

const hasSelectedPet = computed(() => Boolean(selectedPetId.value))

const diaryWeekdays = ['日', '一', '二', '三', '四', '五', '六']

const diaryEntryMap = computed(() => {
  return diaryEntries.value.reduce((map, entry) => {
    map[formatDate(entry.diaryDate)] = entry
    return map
  }, {})
})

const diaryMonthLabel = computed(() => {
  return `${diaryMonth.value.getFullYear()}年${diaryMonth.value.getMonth() + 1}月`
})

const selectedDiaryEntry = computed(() => diaryEntryMap.value[selectedDiaryDate.value] || null)

const diaryCalendarCells = computed(() => buildDiaryCalendarCells(diaryMonth.value, diaryEntryMap.value, selectedDiaryDate.value))

const diaryHint = computed(() => {
  if (!hasSelectedPet.value) {
    return '先选择宠物，再记录日记。'
  }
  if (selectedDiaryEntry.value) {
    return '修改后点击保存即可覆盖当天记录。'
  }
  return '同一天再次保存会覆盖当天记录。'
})

function formatDate(date) {
  if (typeof date === 'string' && /^\d{4}-\d{2}-\d{2}/.test(date)) {
    return date.slice(0, 10)
  }
  const value = new Date(date)
  const year = value.getFullYear()
  const month = String(value.getMonth() + 1).padStart(2, '0')
  const day = String(value.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function createLocalDate(date) {
  if (typeof date === 'string' && /^\d{4}-\d{2}-\d{2}/.test(date)) {
    const [year, month, day] = date.slice(0, 10).split('-').map(Number)
    return new Date(year, month - 1, day)
  }
  return new Date(date)
}

function startOfMonth(date) {
  const value = createLocalDate(date)
  return new Date(value.getFullYear(), value.getMonth(), 1)
}

const buildDiaryCalendarCells = (monthDate, entryMap, selectedDate) => {
  const firstDay = startOfMonth(monthDate)
  const year = firstDay.getFullYear()
  const month = firstDay.getMonth()
  const leadingDays = firstDay.getDay()
  const daysInMonth = new Date(year, month + 1, 0).getDate()
  const cells = []

  for (let index = 0; index < leadingDays; index += 1) {
    cells.push({
      key: `blank-start-${year}-${month}-${index}`,
      inMonth: false,
      dayLabel: '',
      hasEntry: false,
      classes: { blank: true }
    })
  }

  for (let day = 1; day <= daysInMonth; day += 1) {
    const dateKey = formatDate(new Date(year, month, day))
    const entry = entryMap[dateKey]
    const hasEntry = Boolean(entry)
    cells.push({
      key: dateKey,
      dateKey,
      inMonth: true,
      dayLabel: String(day),
      hasEntry,
      isFavorite: Boolean(entry?.favorite),
      classes: {
        selected: dateKey === selectedDate,
        today: dateKey === today,
        'has-entry': hasEntry,
        favorite: Boolean(entry?.favorite)
      }
    })
  }

  while (cells.length % 7 !== 0 || cells.length < 35) {
    cells.push({
      key: `blank-end-${year}-${month}-${cells.length}`,
      inMonth: false,
      dayLabel: '',
      hasEntry: false,
      classes: { blank: true }
    })
  }

  return cells
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
    if (pets.value.length > 0 && !selectedPetId.value) {
      selectedPetId.value = String(pets.value[0].id)
    }
  } catch (error) {
    console.error('获取宠物列表失败', error)
  }
}

const parseDiaryImages = (value) => {
  if (!value) return []
  try {
    const parsed = JSON.parse(value)
    return Array.isArray(parsed) ? parsed : []
  } catch (error) {
    return []
  }
}

const stringifyDiaryImages = () => JSON.stringify(diaryImages.value)

const loadDiaryDraftForDate = (dateKey) => {
  const entry = diaryEntryMap.value[dateKey]
  diaryContent.value = entry?.content || ''
  diaryImages.value = parseDiaryImages(entry?.images)
  diaryFavorite.value = Boolean(entry?.favorite)
}

const selectDiaryDate = (dateKey, { preserveStatus = false } = {}) => {
  if (!dateKey) return
  selectedDiaryDate.value = dateKey
  if (!preserveStatus) {
    diaryStatus.value = ''
  }
  loadDiaryDraftForDate(dateKey)
}

const shiftDiaryMonth = (offset) => {
  const baseDate = createLocalDate(diaryMonth.value)
  const nextMonth = new Date(baseDate.getFullYear(), baseDate.getMonth() + offset, 1)
  diaryMonth.value = nextMonth
  const currentDay = createLocalDate(selectedDiaryDate.value).getDate()
  const daysInMonth = new Date(nextMonth.getFullYear(), nextMonth.getMonth() + 1, 0).getDate()
  const nextDate = formatDate(new Date(nextMonth.getFullYear(), nextMonth.getMonth(), Math.min(currentDay, daysInMonth)))
  selectDiaryDate(nextDate)
}

const fetchDiaryEntries = async () => {
  if (!selectedPetId.value) {
    diaryEntries.value = []
    const fallbackDate = formatDate(new Date())
    diaryMonth.value = startOfMonth(fallbackDate)
    selectDiaryDate(fallbackDate)
    return
  }

  try {
    const response = await axios.get(`/api/pet-diaries/${selectedPetId.value}`)
    diaryEntries.value = Array.isArray(response.data) ? response.data : []
    const currentDateKey = selectedDiaryDate.value
    const hasCurrentDate = diaryEntries.value.some(entry => formatDate(entry.diaryDate) === currentDateKey)
    const preferredDate = hasCurrentDate
      ? currentDateKey
      : (diaryEntries.value[0] ? formatDate(diaryEntries.value[0].diaryDate) : formatDate(new Date()))
    diaryMonth.value = startOfMonth(preferredDate)
    selectDiaryDate(preferredDate)
  } catch (error) {
    console.error('获取宠物日记失败', error)
    diaryEntries.value = []
    const fallbackDate = formatDate(new Date())
    diaryMonth.value = startOfMonth(fallbackDate)
    selectDiaryDate(fallbackDate)
    diaryStatus.value = '加载宠物日记失败'
  }
}

const saveDiaryEntry = async ({ allowEmpty = false, successText = '日记已保存', preserveStatus = true } = {}) => {
  if (!selectedPetId.value) return false
  if (!allowEmpty && !diaryContent.value.trim() && diaryImages.value.length === 0 && !diaryFavorite.value) {
    diaryStatus.value = '请填写日记内容或添加照片'
    return false
  }

  try {
    diarySaving.value = true
    const response = await axios.post('/api/pet-diaries', {
      petId: Number(selectedPetId.value),
      diaryDate: selectedDiaryDate.value,
      content: diaryContent.value.trim(),
      images: stringifyDiaryImages(),
      favorite: diaryFavorite.value
    })
    const savedEntry = response.data
    diaryEntries.value = [
      savedEntry,
      ...diaryEntries.value.filter(entry => formatDate(entry.diaryDate) !== formatDate(savedEntry.diaryDate))
    ].sort((a, b) => formatDate(b.diaryDate).localeCompare(formatDate(a.diaryDate)))
    diaryFavorite.value = Boolean(savedEntry.favorite)
    diaryStatus.value = successText
    selectDiaryDate(formatDate(savedEntry.diaryDate), { preserveStatus })
    return true
  } catch (error) {
    console.error('保存宠物日记失败', error)
    diaryStatus.value = '保存失败，请稍后重试'
    return false
  } finally {
    diarySaving.value = false
  }
}

const toggleFavorite = async () => {
  if (!hasSelectedPet.value) return

  const nextValue = !diaryFavorite.value
  diaryFavorite.value = nextValue
  diaryStatus.value = nextValue ? '收藏中...' : '取消收藏中...'
  const saved = await saveDiaryEntry({
    allowEmpty: true,
    successText: nextValue ? '已收藏' : '已取消收藏',
    preserveStatus: true
  })

  if (!saved) {
    diaryFavorite.value = !nextValue
  }
}

const handleDiaryImagesChange = async (event) => {
  if (!selectedPetId.value) {
    event.target.value = ''
    return
  }

  const selectedFiles = Array.from(event.target.files || [])
  const remainingSlots = Math.max(0, 6 - diaryImages.value.length)
  if (remainingSlots === 0) {
    diaryStatus.value = '最多添加 6 张照片'
    event.target.value = ''
    return
  }

  const files = selectedFiles.slice(0, remainingSlots)
  if (files.length === 0) {
    event.target.value = ''
    return
  }

  try {
    diaryStatus.value = '照片处理中...'
    const images = await Promise.all(
      files.map(file => fileToCompressedDataUrl(file, 900, 0.72))
    )
    diaryImages.value = [...diaryImages.value, ...images]
    diaryStatus.value = selectedFiles.length > files.length
      ? `已添加前 ${files.length} 张照片`
      : '照片已添加'
  } catch (error) {
    diaryStatus.value = error.message || '图片读取失败'
  } finally {
    event.target.value = ''
  }
}

const removeDiaryImage = (index) => {
  diaryImages.value = diaryImages.value.filter((_, itemIndex) => itemIndex !== index)
  diaryStatus.value = '照片已移除，点击保存后生效'
}

watch(selectedPetId, () => {
  fetchDiaryEntries()
})

onMounted(() => {
  fetchPets()
})
</script>

<style scoped>
.diary-page {
  max-width: 1280px;
  margin: 0 auto;
  padding: 20px;
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

.diary-selector {
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

.diary-selector select {
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

.diary-selector svg {
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

.notice,
.empty-state {
  margin-bottom: 24px;
}

.empty-state {
  display: grid;
  place-items: center;
  min-height: 320px;
  padding: 28px;
  border: 1px dashed #cdd8d1;
  border-radius: 12px;
  background-color: #f7faf8;
  text-align: center;
  color: #66766c;
}

.empty-state h2 {
  margin: 12px 0 8px;
  color: #1f2d26;
}

.empty-state p {
  max-width: 360px;
  line-height: 1.6;
}

.empty-state a,
.notice a {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 36px;
  padding: 0 14px;
  margin-top: 14px;
  border-radius: 10px;
  background-color: #2e7d52;
  color: white;
  text-decoration: none;
  font-weight: 700;
}

.notice {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding: 16px 18px;
  border: 1px solid #e0e7e3;
  border-radius: 12px;
  background-color: #ffffff;
}

.diary-content {
  display: grid;
  grid-template-columns: minmax(0, 0.95fr) minmax(0, 1.35fr);
  gap: 24px;
  align-items: start;
}

.diary-content.placeholder .diary-calendar,
.diary-content.placeholder .diary-editor {
  background-color: #fbfcfb;
}

.diary-calendar,
.diary-editor {
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  border: 1px solid #e0e7e3;
}

.calendar-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.calendar-head strong {
  color: #1f2d26;
  font-size: 15px;
}

.calendar-nav {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  margin-top: 0;
  padding: 0;
  border: 1px solid #d7e1db;
  border-radius: 10px;
  background-color: #ffffff;
  color: #2e7d52;
}

.calendar-nav:hover {
  background-color: #edf7ef;
}

.calendar-nav svg {
  width: 16px;
  height: 16px;
  fill: none;
  stroke: currentColor;
  stroke-width: 2.4;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.calendar-weekdays,
.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  gap: 8px;
}

.calendar-weekdays {
  margin-bottom: 10px;
}

.calendar-weekdays span {
  text-align: center;
  font-size: 12px;
  color: #75837c;
  font-weight: 700;
}

.calendar-cell {
  position: relative;
  min-height: 58px;
  margin-top: 0;
  padding: 8px 6px;
  border: 1px solid #dfe8e2;
  border-radius: 10px;
  background-color: #ffffff;
  color: #2b3a31;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.calendar-cell:hover:not(:disabled) {
  border-color: #a7d2b9;
  background-color: #f3fbf6;
}

.calendar-cell:disabled {
  cursor: default;
  opacity: 0.45;
  background-color: #f4f6f5;
}

.calendar-cell.blank {
  border-style: dashed;
  background-color: #f5f8f6;
}

.calendar-cell strong {
  font-size: 15px;
}

.calendar-cell small {
  font-size: 11px;
  font-weight: 700;
  color: #2e7d52;
}

.calendar-cell.has-entry {
  border-color: #9ed0b1;
  background-color: #edf8f1;
}

.calendar-cell.favorite:not(.selected) {
  border-color: #f0c96a;
  background-color: #fff9e8;
}

.calendar-cell.today {
  box-shadow: inset 0 0 0 1px #5f9f74;
}

.calendar-cell.selected {
  border-color: #2e7d52;
  background-color: #2e7d52;
  color: #ffffff;
}

.calendar-cell.selected small {
  color: rgba(255, 255, 255, 0.9);
}

.calendar-cell.selected.today {
  box-shadow: inset 0 0 0 2px rgba(255, 255, 255, 0.55);
}

.calendar-favorite {
  position: absolute;
  top: 4px;
  right: 4px;
  color: #f59e0b;
  font-size: 12px;
  line-height: 1;
  text-shadow: 0 0 1px rgba(255, 255, 255, 0.65);
}

.diary-editor-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.diary-head-tools {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.diary-editor-head p {
  color: #60756a;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.08em;
  margin-bottom: 4px;
  text-transform: uppercase;
}

.diary-editor-head h2 {
  color: #1f2d26;
  margin: 0;
}

.entry-status {
  padding: 6px 10px;
  border-radius: 999px;
  background-color: #eff4f1;
  color: #5f6f66;
  font-size: 12px;
  font-weight: 700;
}

.entry-status.saved {
  background-color: #edf7ef;
  color: #2e7d52;
}

.favorite-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-height: 32px;
  margin-top: 0;
  padding: 0 10px;
  border: 1px solid #f2d28a;
  border-radius: 999px;
  background-color: #fff9e8;
  color: #9a6700;
  font-size: 12px;
  font-weight: 700;
}

.favorite-btn:hover {
  background-color: #fff3cd;
}

.favorite-btn.active {
  border-color: #f0b84d;
  background-color: #fff0bf;
  color: #8f5e00;
}

.favorite-btn:disabled {
  background-color: #f5f8f6;
  border-color: #d7e1db;
  color: #9aa8a0;
}

.diary-editor textarea {
  width: 100%;
  min-height: 160px;
  margin-top: 0;
  padding: 14px 15px;
  border: 1px solid #dce5df;
  border-radius: 10px;
  background-color: #fbfdfb;
  color: #25332b;
  font-size: 14px;
  line-height: 1.65;
  resize: vertical;
}

.diary-editor textarea:disabled {
  background-color: #f7faf8;
  color: #9aa8a0;
}

.diary-image-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  margin-top: 14px;
}

.diary-image-actions span {
  color: #66766c;
  font-size: 12px;
  font-weight: 700;
}

.file-input {
  position: absolute;
  width: 1px;
  height: 1px;
  opacity: 0;
  pointer-events: none;
}

.image-add-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 36px;
  padding: 0 14px;
  border: 1px solid #d7e1db;
  border-radius: 10px;
  background-color: #f5f8f6;
  color: #2f4137;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
}

.image-add-btn.disabled {
  opacity: 0.55;
  pointer-events: none;
}

.image-add-btn:hover {
  background-color: #edf7ef;
}

.diary-images {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(110px, 1fr));
  gap: 10px;
  margin-top: 14px;
}

.diary-image {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.diary-image img {
  width: 100%;
  aspect-ratio: 1 / 1;
  object-fit: cover;
  border-radius: 10px;
  border: 1px solid #dce5df;
}

.diary-image button {
  margin-top: 0;
  padding: 6px 10px;
  border-radius: 8px;
  background-color: #eef2ef;
  color: #4d5f56;
}

.diary-image button:hover {
  background-color: #e2e8e4;
}

.diary-photo-empty {
  margin-top: 14px;
  padding: 14px;
  border: 1px dashed #d2ddd6;
  border-radius: 10px;
  background-color: #f9fbfa;
  color: #77857d;
  font-size: 13px;
  line-height: 1.6;
}

.diary-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  margin-top: 16px;
}

.diary-actions button {
  margin-top: 0;
}

.diary-actions p {
  margin: 0;
  color: #66766c;
  font-size: 13px;
}

@media (max-width: 900px) {
  .page-hero {
    align-items: stretch;
    flex-direction: column;
  }

  .diary-selector,
  .diary-selector select {
    width: 100%;
    min-width: 0;
  }

  .diary-content {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .notice {
    align-items: flex-start;
    flex-direction: column;
  }

  .calendar-weekdays,
  .calendar-grid {
    gap: 6px;
  }

  .calendar-cell {
    min-height: 52px;
  }
}
</style>
