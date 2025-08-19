from playwright.sync_api import sync_playwright

def run(playwright):
    browser = playwright.chromium.launch()
    page = browser.new_page()
    page.goto("http://localhost:5173")

    # Test black player perspective
    page.select_option("select[value=RED]", "BLACK")
    page.click("button[type=submit]")
    page.wait_for_selector(".board")
    page.screenshot(path="jules-scratch/verification/black_perspective.png")

    # Test red player perspective
    page.select_option("select[value=EIGHT_BY_EIGHT]", "EIGHT_BY_EIGHT")
    page.select_option("select[value=BLACK]", "RED")
    page.click("button[type=submit]")
    page.wait_for_selector(".board")
    page.screenshot(path="jules-scratch/verification/red_perspective.png")

    browser.close()

with sync_playwright() as playwright:
    run(playwright)
