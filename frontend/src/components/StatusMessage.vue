<script setup>
defineProps({ error: String, success: String })
</script>

<template>
  <p v-if="error" class="message error">{{ error }}</p>
  <p v-else-if="success" class="message success">{{ success }}</p>
</template>
