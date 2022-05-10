import { ESLBaseElement } from '@exadel/esl/modules/esl-base-element/core';
import { listen } from '@exadel/esl/modules/esl-utils/decorators/listen';
import { attr } from '@exadel/esl/modules/esl-utils/decorators/attr';
import { boolAttr } from '@exadel/esl/modules/esl-utils/decorators/bool-attr';
import { memoize } from '@exadel/esl/modules/esl-utils/decorators/memoize';
import { Point, getTouchPoint } from '@exadel/esl/modules/esl-utils/dom/events/misc';

export class EAKZoomImage extends ESLBaseElement {
  static is = 'eak-zoom-image';

  @attr({ dataAttr: true })
  public src: string;

  @boolAttr()
  public inZoom: boolean;
  @boolAttr()
  public inDrag: boolean;

  @attr({ defaultValue: '2' }) public scale: string;

  protected startPoint: Point;
  protected startPosition: Point;

  @memoize()
  get $image() {
    const originalImage = document.createElement('esl-image');
    originalImage.setAttribute('data-src', this.src);
    originalImage.setAttribute('lazy', '');
    originalImage.setAttribute('mode', 'fit');
    return originalImage;
  }

  @memoize()
  get $closeButton() {
    const button = document.createElement('button');
    button.setAttribute('class', 'close-button');
    return button;
  }

  connectedCallback(): void {
    super.connectedCallback();
    this.appendChild(this.$image);
    this.appendChild(this.$closeButton);
  }

  @listen('click')
  protected onClick(e: MouseEvent): void {
    if (this.$closeButton.contains(e.target as Node)) {
      this.reset();
    }

    if (this.inDrag || this.inZoom) return;
    this.inZoom = true;

    this.$image.style.transform = `scale(${this.scale})`;
    this.style.width = `${this.$image.offsetWidth}px`;
    this.style.height = `${this.$image.offsetHeight}px`;

    this.$$on('pointerdown', this.onPointerDown);
  }

  protected onPointerDown(e: MouseEvent): void {
    this.inDrag = true;
    this.startPoint = getTouchPoint(e);
    this.startPosition = {
      x: parseInt(this.$image.style.left, 10) || 0,
      y: parseInt(this.$image.style.top, 10) || 0,
    };
    this.$$on('pointermove', this.onPointerMove);
    this.$$on('pointerup', this.onPointerUp);
  }

  protected onPointerUp(): void {
    this.inDrag = false;
    this.$$off(this.onPointerMove);
    this.$$off(this.onPointerUp);
  }

  protected onPointerMove(e: MouseEvent): void {
    e.preventDefault();
    if (!this.inDrag) return;
    const point = getTouchPoint(e);

    const offsetX = point.x - this.startPoint.x;
    const offsetY = point.y - this.startPoint.y;
    const scale = parseInt(this.scale);

    const maxOffsetX = this.$image.offsetWidth * scale - this.offsetWidth;
    const maxOffsetY = this.$image.offsetHeight * scale - this.offsetHeight;

    if (maxOffsetX < 0 || maxOffsetY < 0) return;

    const x = this.startPosition.x + offsetX;
    const y = this.startPosition.y + offsetY;

    const normalizedX = Math.min(0, Math.max(-maxOffsetX, x));
    const normalizedY = Math.min(0, Math.max(-maxOffsetY, y));

    Object.assign(this.$image.style, { top: normalizedY, left: normalizedX });
  }

  protected reset(): void {
    if (!this.inZoom) return;
    this.inZoom = false;
    this.$image.style.removeProperty('transform');
    this.style.width = 'auto';
    this.style.height = 'auto';

    this.$$off(this.onPointerDown);
  }

  @listen({ event: 'click', target: 'body' })
  protected onOutsideClick(e: MouseEvent): void {
    if (!this.$image.contains(e.target as Node)) {
      this.reset();
    }
  }
}
