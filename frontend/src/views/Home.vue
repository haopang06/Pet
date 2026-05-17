<template>
  <div class="home">
    <h1>宠物智能喂养系统</h1>
    <p>欢迎使用宠物智能喂养系统，为您的宠物提供科学的喂养方案和健康监测。</p>
    <div class="features">
      <div class="feature" @click="navigateTo('/pets')">
        <h2>宠物管理</h2>
        <p>添加和管理您的宠物信息，为智能喂养做准备。</p>
      </div>
      <div class="feature" @click="navigateTo('/feeding')">
        <h2>智能喂养方案</h2>
        <p>根据宠物的品种、年龄、体重等因素，生成个性化的喂养方案。</p>
      </div>
      <div class="feature" @click="navigateTo('/health')">
        <h2>健康监测</h2>
        <p>持续追踪宠物的健康数据，及时发现异常情况。</p>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  methods: {
    navigateTo(path) {
      this.$router.push(path)
    }
  }
}
</script>

<style scoped>
.home {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  text-align: center;
}

h1 {
  color: #333;
  margin-bottom: 20px;
}

p {
  color: #666;
  margin-bottom: 40px;
  font-size: 18px;
}

.features {
  display: flex;
  justify-content: space-around;
  margin-top: 40px;
}

.feature {
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  flex: 1;
  margin: 0 10px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.feature:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0,0,0,0.1);
}

.feature h2 {
  color: #4CAF50;
  margin-bottom: 10px;
}

.feature p {
  font-size: 14px;
  color: #666;
}
</style>