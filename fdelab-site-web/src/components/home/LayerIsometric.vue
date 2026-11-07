<template>
  <div class="layerfig">
    <svg
      class="lf-svg"
      viewBox="0 0 500 440"
      role="img"
      aria-label="FDE 交付五步，从上到下依次是：跟岗调研、POC 定义、小范围试点、工程化部署、交接与演进"
      @mouseleave="show(-1)"
    >
      <defs>
        <linearGradient id="plateTop" x1="0" y1="0" x2=".2" y2="1">
          <stop offset="0" stop-color="#ffffff" /><stop offset="1" stop-color="#dde9f7" />
        </linearGradient>
        <linearGradient id="flowGrad" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0" stop-color="#2a5fa8" stop-opacity="0" />
          <stop offset=".45" stop-color="#2a5fa8" stop-opacity=".35" />
          <stop offset=".85" stop-color="#25589e" stop-opacity=".95" />
          <stop offset="1" stop-color="#25589e" stop-opacity="1" />
        </linearGradient>
        <linearGradient id="plateTopOn" x1="0" y1="0" x2=".2" y2="1">
          <stop offset="0" stop-color="#d2e4fa" /><stop offset="1" stop-color="#a9c9f1" />
        </linearGradient>
      </defs>

      <g class="lay" :class="{ on: active === 0 }" data-name="跟岗调研" data-desc="数据在哪、口径差在哪、判断靠谁" @mouseenter="show(0)" @click="show(0)" @touchstart.passive="onTouch(0)">
        <rect class="hit" x="0" y="1" width="500" height="86" fill="transparent" />
        <text class="ln" x="74" y="48" text-anchor="end">跟岗调研</text>
        <g class="plate">
          <polygon class="side-a" points="86,51 386,71 386,83 86,63" />
          <polygon class="side-b" points="474,37 386,71 386,83 474,49" />
          <polygon class="top" points="174,17 474,37 386,71 86,51" />
          <polygon class="win" points="144.0,32.0 227.2,37.6 195.2,49.6 112.0,44.0" />
          <polygon class="bar" points="150.4,36.0 212.8,40.0 200.0,44.8 137.6,36.8" />
          <line class="dash" x1="362" y1="27" x2="426" y2="31" />
          <line class="dash" x1="370" y1="44" x2="422" y2="47" />
          <line class="dash" x1="378" y1="59" x2="418" y2="61" />
        </g>
      </g>

      <g class="lay" :class="{ on: active === 1 }" data-name="POC 定义" data-desc="成功标准可量化，停下来的条件写死" @mouseenter="show(1)" @click="show(1)" @touchstart.passive="onTouch(1)">
        <rect class="hit" x="0" y="87" width="500" height="86" fill="transparent" />
        <text class="ln" x="74" y="134" text-anchor="end">POC 定义</text>
        <g class="plate">
          <polygon class="side-a" points="86,137 386,157 386,169 86,149" />
          <polygon class="side-b" points="474,123 386,157 386,169 474,135" />
          <polygon class="top" points="174,103 474,123 386,157 86,137" />
          <polygon class="win" points="144.0,118.0 227.2,123.6 195.2,135.6 112.0,130.0" />
          <polygon class="bar" points="150.4,122.0 212.8,126.0 200.0,130.8 137.6,122.8" />
          <line class="dash" x1="362" y1="113" x2="426" y2="117" />
          <line class="dash" x1="370" y1="130" x2="422" y2="133" />
          <line class="dash" x1="378" y1="145" x2="418" y2="147" />
        </g>
      </g>

      <g class="lay" :class="{ on: active === 2 }" data-name="小范围试点" data-desc="真实业务跑起来，看采纳率不看准确率" @mouseenter="show(2)" @click="show(2)" @touchstart.passive="onTouch(2)">
        <rect class="hit" x="0" y="173" width="500" height="86" fill="transparent" />
        <text class="ln" x="74" y="220" text-anchor="end">小范围试点</text>
        <g class="plate">
          <polygon class="side-a" points="86,223 386,243 386,255 86,235" />
          <polygon class="side-b" points="474,209 386,243 386,255 474,221" />
          <polygon class="top" points="174,189 474,209 386,243 86,223" />
          <polygon class="win" points="144.0,204.0 227.2,209.6 195.2,221.6 112.0,216.0" />
          <polygon class="bar" points="150.4,208.0 212.8,212.0 200.0,216.8 137.6,208.8" />
          <line class="dash" x1="362" y1="199" x2="426" y2="203" />
          <line class="dash" x1="370" y1="216" x2="422" y2="219" />
          <line class="dash" x1="378" y1="231" x2="418" y2="233" />
        </g>
      </g>

      <g class="lay" :class="{ on: active === 3 }" data-name="工程化部署" data-desc="能回滚、能值守，运维自己跑得动" @mouseenter="show(3)" @click="show(3)" @touchstart.passive="onTouch(3)">
        <rect class="hit" x="0" y="259" width="500" height="86" fill="transparent" />
        <text class="ln" x="74" y="306" text-anchor="end">工程化部署</text>
        <g class="plate">
          <polygon class="side-a" points="86,309 386,329 386,341 86,321" />
          <polygon class="side-b" points="474,295 386,329 386,341 474,307" />
          <polygon class="top" points="174,275 474,295 386,329 86,309" />
          <polygon class="win" points="144.0,290.0 227.2,295.6 195.2,307.6 112.0,302.0" />
          <polygon class="bar" points="150.4,294.0 212.8,298.0 200.0,302.8 137.6,294.8" />
          <line class="dash" x1="362" y1="285" x2="426" y2="289" />
          <line class="dash" x1="370" y1="302" x2="422" y2="305" />
          <line class="dash" x1="378" y1="317" x2="418" y2="319" />
        </g>
      </g>

      <g class="lay" :class="{ on: active === 4 }" data-name="交接与演进" data-desc="我们撤场，你的人还能改规则" @mouseenter="show(4)" @click="show(4)" @touchstart.passive="onTouch(4)">
        <rect class="hit" x="0" y="345" width="500" height="86" fill="transparent" />
        <text class="ln" x="74" y="392" text-anchor="end">交接与演进</text>
        <g class="plate">
          <polygon class="side-a" points="86,395 386,415 386,427 86,407" />
          <polygon class="side-b" points="474,381 386,415 386,427 474,393" />
          <polygon class="top" points="174,361 474,381 386,415 86,395" />
          <polygon class="win" points="144.0,376.0 227.2,381.6 195.2,393.6 112.0,388.0" />
          <polygon class="bar" points="150.4,380.0 212.8,384.0 200.0,388.8 137.6,380.8" />
          <line class="dash" x1="362" y1="371" x2="426" y2="375" />
          <line class="dash" x1="370" y1="388" x2="422" y2="391" />
          <line class="dash" x1="378" y1="403" x2="418" y2="405" />
        </g>
      </g>

      <line class="lf-axis" x1="280" y1="12" x2="280" y2="436" />
      <g class="lf-flow" aria-hidden="true">
        <rect class="flow-tip" x="278.4" y="10" width="3.2" height="30" rx="1.6" />
      </g>
      <rect class="lf-node" style="animation-delay:1.553s" x="276.25" y="36.75" width="7.5" height="7.5" rx="1.5" />
      <rect class="lf-node" style="animation-delay:2.7s" x="276.25" y="122.75" width="7.5" height="7.5" rx="1.5" />
      <rect class="lf-node" style="animation-delay:3.847s" x="276.25" y="208.75" width="7.5" height="7.5" rx="1.5" />
      <rect class="lf-node" style="animation-delay:4.993s" x="276.25" y="294.75" width="7.5" height="7.5" rx="1.5" />
      <rect class="lf-node" style="animation-delay:6.14s" x="276.25" y="380.75" width="7.5" height="7.5" rx="1.5" />
      <rect class="lf-node cap" x="275.5" y="7.5" width="9" height="9" rx="2" />
      <rect class="lf-node cap" x="275.5" y="431.5" width="9" height="9" rx="2" />
    </svg>
    <p class="lf-readout" :class="{ hot: active >= 0 }">
      <b>{{ readName }}</b><span>{{ readDesc }}</span>
    </p>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'

const LAYERS = [
  { name: '跟岗调研', desc: '数据在哪、口径差在哪、判断靠谁' },
  { name: 'POC 定义', desc: '成功标准可量化，停下来的条件写死' },
  { name: '小范围试点', desc: '真实业务跑起来，看采纳率不看准确率' },
  { name: '工程化部署', desc: '能回滚、能值守，运维自己跑得动' },
  { name: '交接与演进', desc: '我们撤场，你的人还能改规则' },
]

const DEF_NAME = '陪跑到底，就是这五步'
const DEF_DESC = '悬停任一步，看它交付什么'

const active = ref(-1)
const readName = computed(() => (active.value < 0 ? DEF_NAME : LAYERS[active.value].name))
const readDesc = computed(() => (active.value < 0 ? DEF_DESC : LAYERS[active.value].desc))

const canHover = typeof window !== 'undefined'
  && window.matchMedia('(hover: hover) and (pointer: fine)').matches
const reduce = typeof window !== 'undefined'
  && window.matchMedia('(prefers-reduced-motion: reduce)').matches

let timer = 0
let cursor = 0

function show(index) {
  if (index === active.value) return
  active.value = index
}

function onTouch(index) {
  clearInterval(timer)
  timer = 0
  show(index)
}

onMounted(() => {
  if (canHover || reduce) return
  timer = window.setInterval(() => {
    show(cursor % LAYERS.length)
    cursor += 1
  }, 2400)
})

onBeforeUnmount(() => {
  clearInterval(timer)
})
</script>

<style scoped>
.layerfig {
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.lf-svg {
  width: 100%;
  height: auto;
  flex: 0 0 auto;
  display: block;
  overflow: visible;
}

.lay .top {
  fill: url(#plateTop);
  stroke: #aec2db;
  stroke-width: 1;
  transition: fill .28s ease, stroke .28s ease;
}

.lay .side-a {
  fill: #cfdcee;
  transition: fill .28s ease;
}

.lay .side-b {
  fill: #b2c5dd;
  transition: fill .28s ease;
}

.lay .win {
  fill: #fbfdff;
  stroke: #c0d2e9;
  stroke-width: 1;
  transition: fill .28s ease, stroke .28s ease;
}

.lay .bar {
  fill: #dbe7f6;
  transition: fill .28s ease;
}

.lay .dash {
  stroke: #b7c9df;
  stroke-width: 1.2;
  stroke-dasharray: 3 3;
  transition: stroke .28s ease;
}

.lay .ln {
  fill: #465270;
  font-size: 12px;
  letter-spacing: .06em;
  transition: fill .28s ease;
  font-weight: 600;
}

.lay .plate {
  transition: transform .34s cubic-bezier(.2, .7, .3, 1), filter .28s ease;
  transform-box: fill-box;
  transform-origin: center;
  filter: drop-shadow(0 12px 20px rgba(58, 94, 164, .20));
}

.lay.on .top {
  fill: url(#plateTopOn);
  stroke: #2e6bff;
  stroke-width: 1.6;
}

.lay.on .side-a {
  fill: #b7cfef;
}

.lay.on .side-b {
  fill: #a6c1e6;
}

.lay.on .win {
  fill: #f2f7ff;
  stroke: #a8c6f6;
}

.lay.on .bar {
  fill: #bed4f6;
}

.lay.on .dash {
  stroke: #a8c5f2;
}

.lay.on .ln {
  fill: var(--accent);
}

.lay.on .plate {
  transform: translateY(-2px);
  filter: drop-shadow(0 14px 20px rgba(46, 107, 255, .18));
}

/* 轴线（在板子下层，穿板时被压暗） */
.lf-axis {
  stroke: #4a86e8;
  stroke-width: 2.3;
  stroke-linecap: round;
}

/* 沿轴向下走的电流：深色短段 + 淡蓝头部方块 */
.lf-flow {
  animation: lfFlow 8s linear infinite;
}

.lf-flow .flow-tip {
  fill: url(#flowGrad);
}

@keyframes lfFlow {
  0%, 6% {
    transform: translateY(-16px);
    opacity: 0;
  }

  12% {
    transform: translateY(-16px);
    opacity: 1;
  }

  78% {
    transform: translateY(392px);
    opacity: 1;
  }

  84%, 100% {
    transform: translateY(392px);
    opacity: 0;
  }
}

.lf-node {
  fill: #2f6fd6;
  stroke: none;
  transform-box: fill-box;
  transform-origin: center;
  animation: lfNode 8s linear infinite;
}

.lf-node.cap {
  fill: #2e6bff;
  animation: none;
}

@keyframes lfNode {
  0% {
    transform: scale(1);
  }

  2.5% {
    transform: scale(1.3);
  }

  7% {
    transform: scale(1);
  }

  100% {
    transform: scale(1);
  }
}

@media (prefers-reduced-motion: reduce) {
  .lf-flow,
  .lf-node {
    animation: none;
  }

  .lf-flow {
    opacity: 0;
  }
}

.lf-cap {
  font-size: 10.5px;
  letter-spacing: .12em;
  fill: var(--accent);
  font-weight: 500;
}

.lf-sub {
  font-size: 10.5px;
  letter-spacing: .04em;
  fill: #9aa3b8;
}

.lf-read {
  font-size: 12px;
  fill: var(--ink);
  letter-spacing: .02em;
  font-weight: 500;
}

.lf-readout {
  margin-top: 4px;
  display: flex;
  align-items: baseline;
  gap: 10px;
  min-height: 18px;
}

.lf-readout b {
  font-size: 13px;
  font-weight: 500;
  color: var(--ink);
  letter-spacing: .02em;
  transition: color .28s ease;
}

.lf-readout span {
  font-size: 12.5px;
  color: var(--muted);
  letter-spacing: .03em;
  transition: color .28s ease;
}

.lf-readout.hot b {
  color: var(--accent);
}

@media (max-width: 1000px) {
  .layerfig {
    max-width: 520px;
  }
}

@media (max-width: 640px) {
  .layerfig {
    max-width: 100%;
  }

  .lf-readout {
    flex-direction: column;
    gap: 3px;
  }

  /* 窄屏时图会被等比缩小，层名在矢量内放大以保住可读性 */
  .lay .ln {
    font-size: 13px;
    letter-spacing: .04em;
  }

  .lf-readout b {
    font-size: 13px;
  }

  .lf-readout span {
    font-size: 12px;
  }
}
</style>
