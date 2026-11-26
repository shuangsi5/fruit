<template>
  <div class="dialog-mask" v-if="visible" @click.self="onCancel">
    <div class="dialog">
      <div class="dialog-title">{{ title }}</div>
      <div class="dialog-body">{{ message }}</div>
      <div class="dialog-footer">
        <button class="btn btn-plain" @click="onCancel">{{ cancelText }}</button>
        <button class="btn" :class="danger ? 'btn-danger' : 'btn-primary'" @click="onConfirm">
          {{ confirmText }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  visible: { type: Boolean, default: false },
  title: { type: String, default: '提示' },
  message: { type: String, default: '确定要执行此操作吗？' },
  confirmText: { type: String, default: '确定' },
  cancelText: { type: String, default: '取消' },
  danger: { type: Boolean, default: false }
})
const emit = defineEmits(['confirm', 'cancel'])

function onConfirm() {
  emit('confirm')
}
function onCancel() {
  emit('cancel')
}
</script>

<style scoped>
.dialog-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  z-index: 500;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}
.dialog {
  width: 100%;
  max-width: 420px;
  background: #fff;
  border-radius: 12px;
  box-shadow: var(--shadow-lg);
  overflow: hidden;
}
.dialog-title {
  padding: 20px 24px 0;
  font-size: 17px;
  font-weight: 700;
}
.dialog-body {
  padding: 16px 24px 24px;
  color: var(--text-secondary);
  font-size: 14px;
}
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 16px 24px;
  border-top: 1px solid var(--border);
}
</style>
