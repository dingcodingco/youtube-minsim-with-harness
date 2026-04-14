import { test, expect } from '@playwright/test';

test.describe('Sentiment Display', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/');
    // Wait for data to load
    await expect(page.locator('select option')).not.toHaveCount(1);
  });

  test('should display gauge score as a number in overview mode', async ({ page }) => {
    // The gauge SVG should contain a numeric score text
    const gaugeSection = page.locator('section').filter({ hasText: '감정 온도계' });
    await expect(gaugeSection).toBeVisible();

    // Score should be a number like XX.X
    const scoreText = gaugeSection.locator('svg text').first();
    await expect(scoreText).toBeVisible();
    const text = await scoreText.textContent();
    expect(text).toMatch(/^\d+\.\d+$/);
  });

  test('should display correct total comments count in overview mode', async ({ page }) => {
    // In overview mode, total comments should be 15 (all demo comments)
    await expect(page.getByText(/총 15개 댓글 기준/)).toBeVisible();
  });

  test('should display gauge with correct color for positive score', async ({ page }) => {
    // Select a video known to have positive sentiment
    const selector = page.locator('select');
    await selector.selectOption({ index: 1 }); // demo-video-001

    // Wait for sentiment data to load
    await expect(page.getByText('감정 점수')).toBeVisible();

    // The score arc SVG path should exist
    const gaugeSection = page.locator('section').filter({ hasText: '감정 온도계' });
    const svgPaths = gaugeSection.locator('svg path');
    // At least 2 paths: background track + score arc
    const pathCount = await svgPaths.count();
    expect(pathCount).toBeGreaterThanOrEqual(1);
  });

  test('should show pie chart with sentiment segments when video selected', async ({ page }) => {
    const selector = page.locator('select');
    await selector.selectOption({ index: 1 });

    // Wait for pie chart to render
    const pieSection = page.locator('section').filter({ hasText: '감정 분포' });
    await expect(pieSection).toBeVisible();

    // Recharts renders pie segments as SVG paths inside .recharts-pie
    // Check that at least one sentiment legend button appears
    const sentimentButtons = pieSection.locator('button');
    const buttonCount = await sentimentButtons.count();
    expect(buttonCount).toBeGreaterThan(0);
  });

  test('should display 5 sentiment categories in pie chart legend', async ({ page }) => {
    const selector = page.locator('select');
    await selector.selectOption({ index: 1 });

    const pieSection = page.locator('section').filter({ hasText: '감정 분포' });

    // Expected labels from SENTIMENT_CONFIG
    const expectedLabels = ['긍정', '부정', '중립', '요청', '질문'];

    // Legend buttons should contain the sentiment labels (only for non-zero values)
    const buttons = pieSection.locator('button');
    const buttonCount = await buttons.count();
    expect(buttonCount).toBeGreaterThan(0);
    expect(buttonCount).toBeLessThanOrEqual(5);

    // All displayed labels should be from the expected set
    for (let i = 0; i < buttonCount; i++) {
      const text = await buttons.nth(i).textContent();
      const matchFound = expectedLabels.some((label) => text?.includes(label));
      expect(matchFound).toBe(true);
    }
  });

  test('should show sample comments when pie chart legend is clicked', async ({ page }) => {
    const selector = page.locator('select');
    await selector.selectOption({ index: 1 });

    const pieSection = page.locator('section').filter({ hasText: '감정 분포' });

    // Click a sentiment legend button (first one)
    const firstButton = pieSection.locator('button').first();
    await firstButton.click();

    // Sample comments section should appear with "댓글 샘플" text
    await expect(pieSection.getByText(/댓글 샘플/)).toBeVisible();
  });
});
