<template>
  <article class="glass-card stat-card">
    <p class="stat-label">{{ label }}</p>
    <h3 class="stat-value">{{ value }}</h3>
    <p class="stat-tip">{{ tip }}</p>
  </article>
</template>

<script setup>
defineProps({
  label: { type: String, required: true },
  value: { type: [String, Number], required: true },
  tip: { type: String, default: '' }
})
</script>

<style scoped>
.stat-card {
  padding: 22px;
  min-height: 148px;
  position: relative;
  overflow: hidden;
}

.stat-card::after {
  content: '';
  position: absolute;
  inset: auto -30px -30px auto;
  width: 100px;
  height: 100px;
  border-radius: 999px;
  background: rgba(47, 143, 91, 0.08);
}

.stat-label {
  margin: 0 0 12px;
  color: var(--muted);
}

.stat-value {
  margin: 0 0 10px;
  font-size: 2rem;
}

.stat-tip {
  margin: 0;
  color: var(--brand-deep);
}
</style>
