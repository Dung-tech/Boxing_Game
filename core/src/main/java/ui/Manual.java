package ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Align;
import util.Constants;

public class Manual {
	private final String[] tabs = {"BÀN PHÍM", "CAMERA AI", "CAMERA POSE", "GYM"};
	private int selectedTab = 0;
	private final GlyphLayout layout = new GlyphLayout();

	private static final float SCALE_TITLE = 1.52f;
	private static final float SCALE_TAB = 0.98f;
	private static final float SCALE_SECTION = 1.16f;
	private static final float SCALE_BODY_MAIN = 1.06f;
	private static final float SCALE_BODY_HINT = 1.00f;
	private static final float SCALE_FOOTER = 0.92f;

	public void reset() {
		selectedTab = 0;
	}

	public void handleTabInput() {
		if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.A)) {
			selectedTab = (selectedTab - 1 + tabs.length) % tabs.length;
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || Gdx.input.isKeyJustPressed(Input.Keys.D)) {
			selectedTab = (selectedTab + 1) % tabs.length;
		}
	}

	public void draw(ShapeRenderer shapeRenderer, SpriteBatch batch, BitmapFont font) {
		float overlayX = 80f;
		float overlayY = 58f;
		float overlayW = Constants.APP_WIDTH - 160f;
		float overlayH = Constants.APP_HEIGHT - 116f;
		float tabY = overlayY + overlayH - 108f;
		float tabW = 185f;
		float tabH = 56f;
		float tabGap = 12f;
		float tabStartX = (Constants.APP_WIDTH - (tabW * tabs.length + tabGap * (tabs.length - 1))) / 2f;
		float bodyX = overlayX + 34f;
		float bodyY = overlayY + 44f;
		float bodyW = overlayW - 68f;
		float bodyH = overlayH - 190f;

		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		shapeRenderer.setColor(0, 0, 0, 0.82f);
		shapeRenderer.rect(0, 0, Constants.APP_WIDTH, Constants.APP_HEIGHT);

		shapeRenderer.setColor(14 / 255f, 18 / 255f, 26 / 255f, 0.98f);
		shapeRenderer.rect(overlayX, overlayY, overlayW, overlayH);

		shapeRenderer.setColor(34 / 255f, 42 / 255f, 58 / 255f, 1f);
		shapeRenderer.rect(bodyX, bodyY, bodyW, bodyH);

		for (int i = 0; i < tabs.length; i++) {
			float x = tabStartX + i * (tabW + tabGap);
			shapeRenderer.setColor(i == selectedTab ? new Color(0.80f, 0.22f, 0.22f, 1f) : new Color(0.22f, 0.28f, 0.38f, 1f));
			shapeRenderer.rect(x, tabY, tabW, tabH);
		}
		shapeRenderer.end();

		batch.begin();
		drawCenter(batch, font, "HƯỚNG DẪN", overlayY + overlayH - 20f, Color.CYAN, SCALE_TITLE);

		font.getData().setScale(SCALE_TAB);
		for (int i = 0; i < tabs.length; i++) {
			layout.setText(font, tabs[i]);
			float tabTextX = tabStartX + i * (tabW + tabGap) + (tabW - layout.width) / 2f;
			font.setColor(i == selectedTab ? Color.WHITE : Color.LIGHT_GRAY);
			font.draw(batch, tabs[i], tabTextX, tabY + 38f);
		}

		float textX = bodyX + 26f;
		float textW = bodyW - 52f;
		float y = bodyY + bodyH - 24f;

		if (selectedTab == 0) {
			y = drawSectionTitle(batch, font, "BÀN PHÍM", textX, y, textW);
			y = drawBullet(batch, font, "P1: D=Đấm | A=Đá | W=Đỡ | S=Né | SPACE=Tuyệt kỹ", textX, y, textW, Color.WHITE);
			y = drawBullet(batch, font, "P2: RIGHT=Đấm | LEFT=Đá | UP=Đỡ | DOWN=Né | ENTER=Tuyệt kỹ", textX, y, textW, Color.WHITE);
			y = drawBullet(batch, font, "Giữ nhịp đánh đều, ưu tiên đỡ và né khi bị ép góc.", textX, y, textW, Color.LIGHT_GRAY);
		} else if (selectedTab == 1) {
			y = drawSectionTitle(batch, font, "CAMERA AI - NGÓN TAY", textX, y, textW);
			y = drawBullet(batch, font, "Mapping: 0=IDLE | 1=Đấm | 2=Đá | 3=Đỡ | 4=Né | 5=Tuyệt kỹ", textX, y, textW, Color.WHITE);
			y = drawBullet(batch, font, "Đứng nửa trái khung hình cho P1, nửa phải cho P2.", textX, y, textW, Color.LIGHT_GRAY);
			y = drawBullet(batch, font, "Trước khi chơi, đợi camera ổn định và nhận diện rõ tay.", textX, y, textW, Color.LIGHT_GRAY);
			y = drawBullet(batch, font, "Nhấn Q để tắt cửa sổ camera.", textX, y, textW, Color.WHITE);
		} else if (selectedTab == 2) {
			y = drawSectionTitle(batch, font, "CAMERA POSE - NỬA THÂN TRÊN", textX, y, textW);
			y = drawBullet(batch, font, "Bắt buộc: camera phải thấy rõ nửa thân trên trước khi đấu.", textX, y, textW, Color.LIGHT_GRAY);
			y = drawBullet(batch, font, "IDLE=guard | BLOCK=2 tay che giữa | DUCK=hạ người", textX, y, textW, Color.WHITE);
			y = drawBullet(batch, font, "PUNCH=đấm xuống rõ | KICK=đấm móc rõ | SKILL=đấm 2 tay", textX, y, textW, Color.WHITE);
			y = drawBullet(batch, font, "Nếu tracking chậm: lùi nhẹ, đứng đủ sáng, tránh quay nhanh.", textX, y, textW, Color.LIGHT_GRAY);
		} else {
			y = drawSectionTitle(batch, font, "GYM", textX, y, textW);
			y = drawBullet(batch, font, "ENTER: Concentric", textX, y, textW, Color.WHITE);
			y = drawBullet(batch, font, "SPACE: Eccentric", textX, y, textW, Color.WHITE);
			y = drawBullet(batch, font, "ESC: Về menu", textX, y, textW, Color.WHITE);
			y = drawBullet(batch, font, "Mục tiêu: luân phiên động tác để giữ HP gymer.", textX, y, textW, Color.LIGHT_GRAY);
			y = drawBullet(batch, font, "Khi game-over: W/S hoặc UP/DOWN để chọn, ENTER để xác nhận.", textX, y, textW, Color.LIGHT_GRAY);
			y = drawBullet(batch, font, "Có thể nhấn ESC để thoát nhanh về menu.", textX, y, textW, Color.LIGHT_GRAY);
		}

		drawCenter(batch, font, "LEFT/RIGHT (hoặc A/D) để đổi phần   |   ESC để đóng", overlayY + 26f, Color.SALMON, SCALE_FOOTER);
		font.getData().setScale(2f);
		batch.end();
	}

	private float drawLine(SpriteBatch batch, BitmapFont font, String text, float x, float y, float width, Color color, float scale) {
		font.getData().setScale(scale);
		layout.setText(font, text, color, width, Align.left, true);
		font.draw(batch, layout, x, y);
		return y - layout.height - 12f;
	}

	private float drawSectionTitle(SpriteBatch batch, BitmapFont font, String text, float x, float y, float width) {
		return drawLine(batch, font, text, x, y, width, Color.GOLD, SCALE_SECTION);
	}

	private float drawBullet(SpriteBatch batch, BitmapFont font, String text, float x, float y, float width, Color color) {
		return drawLine(batch, font, "- " + text, x, y, width, color, SCALE_BODY_MAIN);
	}

	private void drawCenter(SpriteBatch batch, BitmapFont font, String text, float y, Color color, float scale) {
		font.getData().setScale(scale);
		layout.setText(font, text);
		font.setColor(color);
		float x = (Constants.APP_WIDTH - layout.width) / 2f;
		font.draw(batch, text, x, y);
	}
}
