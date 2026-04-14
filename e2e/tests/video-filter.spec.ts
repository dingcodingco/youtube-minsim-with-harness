import { test, expect } from '@playwright/test';

test.describe('Video Filter', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/');
    // Wait for video list to load in the dropdown
    await expect(page.locator('select option')).not.toHaveCount(1);
  });

  test('should select a video and update gauge', async ({ page }) => {
    const selector = page.locator('select');

    // Select first demo video
    await selector.selectOption({ index: 1 });

    // Wait for sentiment data to load - gauge should update
    await expect(page.getByText('감정 점수')).toBeVisible();
    // The total comments text should update to the selected video's count
    await expect(page.getByText(/총.*개 댓글 기준/)).toBeVisible();
  });

  test('should show pie chart when video is selected', async ({ page }) => {
    const selector = page.locator('select');

    // In overview mode, pie chart area shows empty state
    await expect(page.getByText('영상을 선택하면 감정 분포를 확인할 수 있습니다')).toBeVisible();

    // Select a video
    await selector.selectOption({ index: 1 });

    // Empty state should be gone, pie chart legend should appear
    await expect(page.getByText('영상을 선택하면 감정 분포를 확인할 수 있습니다')).not.toBeVisible();

    // Expect at least one sentiment legend label to appear
    const sentimentLabels = ['긍정', '부정', '중립', '요청', '질문'];
    let foundLabel = false;
    for (const label of sentimentLabels) {
      const count = await page.getByText(label, { exact: true }).count();
      if (count > 0) {
        foundLabel = true;
        break;
      }
    }
    expect(foundLabel).toBe(true);
  });

  test('should show keyword ranking when video is selected', async ({ page }) => {
    const selector = page.locator('select');

    // Select a video
    await selector.selectOption({ index: 1 });

    // Keyword empty state should disappear
    await expect(page.getByText('영상을 선택하면 키워드 랭킹을 확인할 수 있습니다')).not.toBeVisible();

    // The keyword section title should still be visible
    await expect(page.getByText('키워드 랭킹 Top 10')).toBeVisible();
  });

  test('should show content request table when video is selected', async ({ page }) => {
    const selector = page.locator('select');

    // Select a video
    await selector.selectOption({ index: 1 });

    // Content request section should update
    await expect(page.getByText('시청자 요청 콘텐츠').first()).toBeVisible();
  });

  test('should return to overview when "전체 채널" is selected', async ({ page }) => {
    const selector = page.locator('select');

    // Select a video first
    await selector.selectOption({ index: 1 });
    await expect(page.getByText('영상을 선택하면 감정 분포를 확인할 수 있습니다')).not.toBeVisible();

    // Return to overview
    await selector.selectOption('');

    // Pie chart should show empty state again
    await expect(page.getByText('영상을 선택하면 감정 분포를 확인할 수 있습니다')).toBeVisible();
  });
});
