const fs = require("fs");
const path = require("path");
const sharpModulePath = process.argv[2];
if (!sharpModulePath) {
  throw new Error("Usage: node scripts/prepare_play_graphics.cjs <sharp-module-path>");
}
const sharp = require(sharpModulePath);

const root = path.resolve(__dirname, "..");
const graphicsDir = path.join(root, "play-console", "graphics");
const iconSvgPath = path.join(root, "docs", "assets", "app-icon.svg");
const appScreenPath = path.join(root, "docs", "assets", "app-screen-v0.1.2.png");

fs.mkdirSync(graphicsDir, { recursive: true });

async function renderIcon() {
  await sharp(iconSvgPath, { density: 1024 })
    .resize(512, 512)
    .png({ compressionLevel: 9 })
    .toFile(path.join(graphicsDir, "app-icon-512.png"));
}

async function renderFeatureGraphic() {
  const phoneWidth = 214;
  const phoneHeight = 464;
  const phone = await sharp(appScreenPath)
    .resize(phoneWidth, phoneHeight, { fit: "cover", position: "top" })
    .composite([{ input: Buffer.from(`<svg width="${phoneWidth}" height="${phoneHeight}"><rect width="${phoneWidth}" height="${phoneHeight}" rx="26" fill="#fff"/></svg>`), blend: "dest-in" }])
    .png()
    .toBuffer();

  const copy = Buffer.from(`
    <svg width="1024" height="500" xmlns="http://www.w3.org/2000/svg">
      <style>
        .title { font: 700 52px "Malgun Gothic", "Noto Sans KR", sans-serif; fill: #1c1c1e; }
        .body { font: 400 25px "Malgun Gothic", "Noto Sans KR", sans-serif; fill: #5a5a60; }
        .label { font: 700 20px "Malgun Gothic", "Noto Sans KR", sans-serif; fill: #0066cc; }
      </style>
      <text x="72" y="205" class="title">한 달 일정을</text>
      <text x="72" y="273" class="title">카톡·문자로 보내세요</text>
      <text x="72" y="332" class="body">일정 읽기 · 텍스트 공유 · TXT 파일</text>
      <text x="72" y="397" class="label">로그인 없이, 휴대폰 안에서 처리</text>
    </svg>`);

  const icon = await sharp(iconSvgPath, { density: 512 }).resize(76, 76).png().toBuffer();
  const phoneFrame = Buffer.from(`<svg width="226" height="476" xmlns="http://www.w3.org/2000/svg"><rect x="0" y="0" width="226" height="476" rx="32" fill="#202124"/></svg>`);

  await sharp({ create: { width: 1024, height: 500, channels: 4, background: "#f2f2f7" } })
    .composite([
      { input: icon, left: 72, top: 61 },
      { input: copy, left: 0, top: 0 },
      { input: phoneFrame, left: 725, top: 12 },
      { input: phone, left: 731, top: 18 },
    ])
    .png({ compressionLevel: 9 })
    .toFile(path.join(graphicsDir, "feature-graphic-1024x500.png"));
}

Promise.all([renderIcon(), renderFeatureGraphic()]).catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
