<template>
  <span class="pet-avatar" :class="[sizeClass, { circle }]">
    <img v-if="pet?.photo" :src="pet.photo" :alt="pet.name || '宠物照片'">
    <PetTypeIcon v-else :type="type" />
  </span>
</template>

<script setup>
import { computed } from 'vue'
import PetTypeIcon from './PetTypeIcon.vue'

const props = defineProps({
  pet: {
    type: Object,
    default: () => ({})
  },
  type: {
    type: String,
    default: 'cat'
  },
  size: {
    type: String,
    default: 'md'
  },
  circle: {
    type: Boolean,
    default: false
  }
})

const sizeClass = computed(() => `size-${props.size}`)
</script>

<style scoped>
.pet-avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 46px;
  height: 46px;
  overflow: hidden;
  border-radius: 10px;
  background-color: #eef6f1;
  flex: 0 0 auto;
}

.pet-avatar.circle {
  border-radius: 50%;
}

.size-sm {
  width: 34px;
  height: 34px;
}

.size-md {
  width: 46px;
  height: 46px;
}

.size-lg {
  width: 62px;
  height: 62px;
}

img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

:deep(.pet-type-icon) {
  width: 100%;
  height: 100%;
  border-radius: inherit;
}

:deep(svg) {
  width: 70%;
  height: 70%;
}
</style>
