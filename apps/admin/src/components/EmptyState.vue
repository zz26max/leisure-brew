<script setup lang="ts">
withDefaults(defineProps<{ title?: string; description?: string }>(), {
  title: '这里还没有内容',
  description: '换个筛选条件，或稍后再来看看。',
})
</script>

<template>
  <div class="empty-state">
    <div class="empty-state__cup"><span /></div>
    <strong>{{ title }}</strong>
    <p>{{ description }}</p>
  </div>
</template>
