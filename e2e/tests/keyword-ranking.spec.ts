import { test, expect } from '@playwright/test';

test.describe('Keyword Ranking', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/');
    // Wait for video list to load
    await expect(page.locator('select option')).not.toHaveCount(1);
  });

  test('should show empty state when no video is selected', async ({ page }) => {
    await expect(page.getByText('영상을 선택하면 키워드 랭킹을 확인할 수 있습니다')).toBeVisible();
  });

  test('should display keyword bar chart when video is selected', async ({ page }) => {
    const selector = page.locator('select');
    await selector.selectOption({ index: 1 });

    // Wait for keyword data to load
    await expect(page.getByText('키워드 랭킹 Top 10')).toBeVisible();

    // Recharts renders bars in SVG - look for the chart container
    const keywordSection = page.locator('section').filter({ hasText: '키워드 랭킹 Top 10' });
    await expect(keywordSection).toBeVisible();

    // Chart should have rendered (recharts wrapper div with SVG)
    const chartContainer = keywordSection.locator('.recharts-wrapper');
    await expect(chartContainer).toBeVisible();
  });

  test('should display keyword labels on Y-axis', async ({ page }) => {
    const selector = page.locator('select');
    await selector.selectOption({ index: 1 });

    const keywordSection = page.locator('section').filter({ hasText: '키워드 랭킹 Top 10' });

    // Y-axis should have keyword text labels
    const yAxisTicks = keywordSection.locator('.recharts-yAxis .recharts-cartesian-axis-tick text');
    const tickCount = await yAxisTicks.count();
    expect(tickCount).toBeGreaterThan(0);
    expect(tickCount).toBeLessThanOrEqual(10);
  });

  test('should display colored bars based on sentiment', async ({ page }) => {
    const selector = page.locator('select');
    await selector.selectOption({ index: 1 });

    const keywordSection = page.locator('section').filter({ hasText: '키워드 랭킹 Top 10' });

    // Bars are rendered as SVG rectangles inside recharts-bar
    const bars = keywordSection.locator('.recharts-bar-rectangle');
    const barCount = await bars.count();
    expect(barCount).toBeGreaterThan(0);
  });

  test('should show keyword data for different videos', async ({ page }) => {
    const selector = page.locator('select');

    // Select first video
    await selector.selectOption({ index: 1 });
    const keywordSection = page.locator('section').filter({ hasText: '키워드 랭킹 Top 10' });
    await expect(keywordSection.locator('.recharts-wrapper')).toBeVisible();

    // Select second video
    await selector.selectOption({ index: 2 });
    // Chart should still be visible but with different data
    await expect(keywordSection.locator('.recharts-wrapper')).toBeVisible();
  });
});
