<template>
  <span class="brand-mark" aria-hidden="true">
    <i class="brand-mark__leaf" />
    <i class="brand-mark__cup" />
  </span>
</template>

<style scoped>
.brand-mark {
  position: relative;
  display: inline-grid;
  width: 42px;
  height: 42px;
  overflow: hidden;
  background: var(--tea-900);
  border-radius: 14px 14px 14px 5px;
  place-items: center;
}
.brand-mark__cup {
  position: absolute;
  bottom: 9px;
  width: 20px;
  height: 12px;
  background: var(--rice-50);
  border-radius: 2px 2px 10px 10px;
}
.brand-mark__leaf {
  position: absolute;
  top: 8px;
  left: 11px;
  width: 14px;
  height: 8px;
  background: var(--jasmine-400);
  border-radius: 100% 0 100% 0;
  transform: rotate(-18deg);
}
</style>
