<template>
  <span class="user-avatar" :class="sizeClass">
    <img v-if="avatar" :src="avatar" :alt="altText">
    <span v-else>{{ fallbackText }}</span>
  </span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  avatar: {
    type: String,
    default: ''
  },
  name: {
    type: String,
    default: ''
  },
  size: {
    type: String,
    default: 'md'
  }
})

const sizeClass = computed(() => `size-${props.size}`)

const fallbackText = computed(() => (props.name ? props.name.slice(0, 1).toUpperCase() : '用'))

const altText = computed(() => props.name ? `${props.name} 的头像` : '用户头像')
</script>

<style scoped>
.user-avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border-radius: 50%;
  background-color: #2e7d52;
  color: white;
  flex: 0 0 auto;
  font-weight: 700;
}

.size-sm {
  width: 32px;
  height: 32px;
  font-size: 13px;
}

.size-md {
  width: 42px;
  height: 42px;
  font-size: 15px;
}

.size-lg {
  width: 72px;
  height: 72px;
  font-size: 24px;
}

.size-xl {
  width: 96px;
  height: 96px;
  font-size: 30px;
}

img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
</style>
