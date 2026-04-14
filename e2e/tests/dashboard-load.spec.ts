import { test, expect } from '@playwright/test';

test.describe('Dashboard Load', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/');
  });

  test('should display the header with YooBe Pulse title', async ({ page }) => {
    await expect(page.getByText('YooBe Pulse')).toBeVisible();
  });

  test('should display the video selector dropdown', async ({ page }) => {
    const selector = page.locator('select');
    await expect(selector).toBeVisible();
    // Default option should be channel overview
    await expect(selector).toHaveValue('');
  });

  test('should display sentiment gauge section', async ({ page }) => {
    await expect(page.getByText('감정 온도계')).toBeVisible();
    // Gauge should show a score
    await expect(page.getByText('감정 점수')).toBeVisible();
    // Should show total comments text
    await expect(page.getByText(/총.*개 댓글 기준/)).toBeVisible();
  });

  test('should display sentiment distribution section', async ({ page }) => {
    await expect(page.getByText('감정 분포')).toBeVisible();
    // In overview mode (no video selected), should show empty state
    await expect(page.getByText('영상을 선택하면 감정 분포를 확인할 수 있습니다')).toBeVisible();
  });

  test('should display trend chart section', async ({ page }) => {
    await expect(page.getByText('감정 트렌드')).toBeVisible();
    // Period toggle buttons should be visible
    await expect(page.getByRole('button', { name: '7일' })).toBeVisible();
    await expect(page.getByRole('button', { name: '14일' })).toBeVisible();
    await expect(page.getByRole('button', { name: '30일' })).toBeVisible();
  });

  test('should display keyword ranking section', async ({ page }) => {
    await expect(page.getByText('키워드 랭킹 Top 10').first()).toBeVisible();
    // In overview mode, should show empty state
    await expect(page.getByText('영상을 선택하면 키워드 랭킹을 확인할 수 있습니다')).toBeVisible();
  });

  test('should display content request section', async ({ page }) => {
    await expect(page.getByText('시청자 요청 콘텐츠').first()).toBeVisible();
  });

  test('should display video options in dropdown', async ({ page }) => {
    const selector = page.locator('select');
    const options = selector.locator('option');
    // Should have "전체 채널" + 3 demo videos = 4 options
    await expect(options).toHaveCount(4);
    await expect(options.first()).toHaveText('전체 채널');
  });
});
