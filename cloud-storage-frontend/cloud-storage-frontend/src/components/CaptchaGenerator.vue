<template>
  <div class="captcha-generator" @click="refreshCaptcha" title="点击刷新验证码">
    <canvas ref="captchaCanvas" :width="width" :height="height"></canvas>
  </div>
</template>

<script setup>
import { ref, onMounted, defineExpose } from 'vue';

const props = defineProps({
  width: { type: Number, default: 120 },
  height: { type: Number, default: 40 },
  minFontSize: { type: Number, default: 22 }, // 最小字体大小
  maxFontSize: { type: Number, default: 32 }, // 最大字体大小
  charNum: { type: Number, default: 4 }, // 验证码字符数量
  chars: {
    type: String,
    default: 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'
  }
});

const captchaCanvas = ref(null);
const captchaCode = ref('');

const randomNum = (min, max) => Math.floor(Math.random() * (max - min + 1) + min);

const randomColor = (min, max) => {
  const r = randomNum(min, max);
  const g = randomNum(min, max);
  const b = randomNum(min, max);
  return `rgb(${r},${g},${b})`;
};

const drawCaptcha = () => {
  const canvas = captchaCanvas.value;
  if (!canvas) return;
  const ctx = canvas.getContext('2d');

  // 1. 填充背景
  ctx.fillStyle = randomColor(240, 255); // 浅色背景
  ctx.fillRect(0, 0, props.width, props.height);

  // 2. 绘制复杂干扰背景 (随机矩形/弧线)
  for (let i = 0; i < 5; i++) {
    ctx.fillStyle = randomColor(200, 240);
    ctx.globalAlpha = Math.random() * 0.5 + 0.2;
    ctx.beginPath();
    const type = randomNum(0, 1);
    if (type === 0) { // 矩形
      ctx.rect(
        randomNum(-props.width / 2, props.width),
        randomNum(-props.height / 2, props.height),
        randomNum(props.width / 4, props.width / 2),
        randomNum(props.height / 4, props.height / 2)
      );
    } else { // 弧线
      ctx.arc(
        randomNum(0, props.width),
        randomNum(0, props.height),
        randomNum(10, 30),
        Math.random() * Math.PI * 2,
        Math.random() * Math.PI * 2
      );
    }
    ctx.fill();
  }
  ctx.globalAlpha = 1; // 重置透明度

  // 3. 生成验证码字符
  let code = '';
  for (let i = 0; i < props.charNum; i++) {
    const char = props.chars[Math.floor(Math.random() * props.chars.length)];
    code += char;
  }
  captchaCode.value = code;

  // 4. 绘制字符 (增加高级感和扭曲)
  for (let i = 0; i < code.length; i++) {
    ctx.fillStyle = randomColor(50, 150);
    ctx.font = `bold italic ${randomNum(props.minFontSize, props.maxFontSize)}px Arial, sans-serif`; // 字体粗细和大小随机
    ctx.textBaseline = 'middle';
    ctx.textAlign = 'center';

    ctx.save();
    
    const charX = (props.width / props.charNum) * i + props.width / (props.charNum * 2) + randomNum(-5, 5); // 增加水平随机位移
    const charY = props.height / 2 + randomNum(-5, 5); // 增加垂直随机位移

    ctx.translate(charX, charY);
    ctx.rotate(randomNum(-30, 30) * Math.PI / 180); // 随机旋转角度

    // 添加更明显的阴影
    ctx.shadowColor = 'rgba(0,0,0,0.5)';
    ctx.shadowBlur = 4;
    ctx.shadowOffsetX = 2;
    ctx.shadowOffsetY = 2;

    ctx.fillText(code[i], 0, 0);

    ctx.restore();
  }

  // 5. 绘制随机干扰线
  for (let i = 0; i < 8; i++) {
    ctx.strokeStyle = randomColor(150, 220);
    ctx.lineWidth = randomNum(1, 2);
    ctx.beginPath();
    ctx.moveTo(randomNum(0, props.width), randomNum(0, props.height));
    ctx.lineTo(randomNum(0, props.width), randomNum(0, props.height));
    ctx.stroke();
  }

  // 6. 绘制随机干扰点 (噪点)
  for (let i = 0; i < 80; i++) {
    ctx.fillStyle = randomColor(100, 200);
    ctx.beginPath();
    ctx.arc(randomNum(0, props.width), randomNum(0, props.height), randomNum(0, 1), 0, 2 * Math.PI);
    ctx.fill();
  }
};

const refreshCaptcha = () => {
  drawCaptcha();
};

onMounted(() => {
  drawCaptcha();
});

defineExpose({
  getCaptchaCode: () => captchaCode.value,
  refreshCaptcha,
});
</script>

<style scoped>
.captcha-generator {
  width: var(--captcha-width);
  height: var(--captcha-height);
  background-color: var(--el-fill-color-light); 
  border-radius: var(--el-border-radius-base);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  user-select: none;
  overflow: hidden;
  border: 1px solid var(--el-border-color-light); 
  box-sizing: border-box;
  /* 增加悬停时的反馈效果 */
  transition: box-shadow 0.2s;
}

.captcha-generator:hover {
  box-shadow: 0 0 0 2px var(--el-color-primary-light-5) inset;
}

canvas {
  display: block; 
}
</style>