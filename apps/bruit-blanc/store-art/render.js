const { chromium } = require(process.env.PW_PATH || 'playwright');
(async () => {
  const [file, w, h, out] = process.argv.slice(2);
  const browser = await chromium.launch();
  const page = await browser.newPage({ viewport: { width: +w, height: +h } });
  await page.goto('file://' + file);
  await page.screenshot({ path: out, omitBackground: false });
  await browser.close();
})();
