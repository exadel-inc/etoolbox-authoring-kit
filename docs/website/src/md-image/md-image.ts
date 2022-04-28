import { attr, boolAttr, ESLBaseElement, listen } from '@exadel/esl/modules/esl-base-element/core';
import { DeviceDetector, getOffsetPoint, getTouchPoint, Point } from '@exadel/esl';
import { memoize } from '@exadel/esl/modules/esl-utils/decorators/memoize';

export class MdImage extends ESLBaseElement {
  static is = 'md-image';
  static zoomClass: string = 'md-image-zoom';

  @attr({ dataAttr: true })
  public src: string;

  @boolAttr()
  public inZoom: boolean;
  @boolAttr()
  public inDrag: boolean;

  public scale: number = 2;
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
    button.onclick = this.onClose.bind(this);
    button.setAttribute('class', 'close-button');
    button.innerText = 'X';
    button.style.display = 'none';
    return button;
  }

  set offsetX(pos: number) {
    pos = Math.min(1, Math.max(0, pos));
    const img = this.$image;
    const maxOffsetX = this.$image.offsetWidth * this.scale - this.offsetWidth;
    if (maxOffsetX < 0) return;
    img.style.left = `${-maxOffsetX * pos}px`;
  }
  set offsetY(pos: number) {
    pos = Math.min(1, Math.max(0, pos));
    const img = this.$image;
    const maxOffsetY = this.$image.offsetHeight * this.scale - this.offsetHeight;
    if (maxOffsetY < 0) return;
    img.style.top = `${-maxOffsetY * pos}px`;
  }

  connectedCallback(): void {
    super.connectedCallback();
    this.appendChild(this.$image);
    this.appendChild(this.$closeButton);
  }

  @listen('click')
  protected onClick(e: MouseEvent): void {
    if (this.inDrag || this.inZoom) return;
    this.inZoom = true;

    this.$image.style.transform = `scale(${this.scale})`;
    this.style.width = `${this.$image.offsetWidth}px`;
    this.style.height = `${this.$image.offsetHeight}px`;

    this.$closeButton.style.display = 'block';
    this.$$on('pointerdown', this.onMouseDown);
  }

  protected onClose(e: MouseEvent): void {
    e.stopPropagation();
    if (!this.inZoom) return;
    this.inZoom = false;

    this.$image.style.removeProperty('transform');
    this.style.width = 'auto';
    this.style.height = 'auto';

    this.$closeButton.style.display = 'none';

    this.$$off(this.onMouseDown);
  }

  protected onMouseDown(e: MouseEvent): void {
    this.inDrag = true;
    this.startPoint = getTouchPoint(e);
    this.startPosition = {
      x: parseInt(this.$image.style.left, 10) || 0,
      y: parseInt(this.$image.style.top, 10) || 0,
    };
    this.$$on({ event: 'pointermove', target: document.body }, this.onMouseMove);
    this.$$on({ event: 'pointerup', target: document.body }, this.onMouseUp);
  }

  protected onMouseUp(): void {
    this.inDrag = false;
    this.$$off(this.onMouseMove);
    this.$$off(this.onMouseUp);
  }

  protected onMouseMove(e: MouseEvent): void {
    if (!this.inDrag) return;
    const point = getTouchPoint(e);

    const offsetX = point.x - this.startPoint.x;
    const offsetY = point.y - this.startPoint.y;

    const maxOffsetX = this.$image.offsetWidth * this.scale - this.offsetWidth;
    const maxOffsetY = this.$image.offsetHeight * this.scale - this.offsetHeight;

    if (maxOffsetX < 0 || maxOffsetY < 0) return;

    const x = this.startPosition.x + offsetX;
    const y = this.startPosition.y + offsetY;

    const normalizedX = Math.min(0, Math.max(-maxOffsetX, x));
    const normalizedY = Math.min(0, Math.max(-maxOffsetY, y));

    Object.assign(this.$image.style, { top: normalizedY, left: normalizedX });
  }
}
