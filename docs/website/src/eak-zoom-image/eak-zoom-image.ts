import {ESLBaseElement} from '@exadel/esl/modules/esl-base-element/core';
import {listen} from '@exadel/esl/modules/esl-utils/decorators/listen';
import {attr} from '@exadel/esl/modules/esl-utils/decorators/attr';
import {boolAttr} from '@exadel/esl/modules/esl-utils/decorators/bool-attr';
import {memoize} from '@exadel/esl/modules/esl-utils/decorators/memoize';
import {getTouchPoint} from '@exadel/esl/modules/esl-utils/dom/events/misc';

import type {Point} from '@exadel/esl/modules/esl-utils/dom/events/misc';

export class EAKZoomImage extends ESLBaseElement {
  static is = 'eak-zoom-image';

  @attr({dataAttr: true}) public src: string;
  @attr({defaultValue: '2'}) public scale: string;

  @boolAttr() public inZoom: boolean;
  @boolAttr() public inDrag: boolean;

  protected startPoint: Point;
  protected startPosition: Point;

  @memoize()
  get $image(): HTMLElement {
    const originalImage = document.createElement('esl-image');
    originalImage.setAttribute('data-src', this.src);
    originalImage.setAttribute('lazy', '');
    originalImage.setAttribute('mode', 'fit');
    return originalImage;
  }

  @memoize()
  get $closeButton(): HTMLElement {
    const button = document.createElement('button');
    button.setAttribute('class', 'close-button');
    return button;
  }

  connectedCallback(): void {
    super.connectedCallback();
    this.appendChild(this.$image);
    this.appendChild(this.$closeButton);
  }

  public zoom(): void {
    if (this.inDrag || this.inZoom) return;
    this.inZoom = true;

    this.$image.style.transform = `scale(${this.scale})`;
    this.$image.style.marginTop = '0';
    this.$image.style.marginLeft = '0';
    this.style.width = `${this.$image.offsetWidth}px`;
    this.style.height = `${this.$image.offsetHeight}px`;

    this.$$on('pointerdown', this.onPointerDown);
  }
  public unzoom(): void {
    this.inZoom = false;

    this.$image.style.removeProperty('transform');
    this.$image.style.marginTop = '0';
    this.$image.style.marginLeft = '0';
    this.style.width = 'auto';
    this.style.height = 'auto';

    this.$$off(this.onPointerDown);
  }

  protected onPointerDown(e: PointerEvent): void {
    this.inDrag = true;
    this.startPoint = getTouchPoint(e);
    this.startPosition = {
      x: parseInt(this.$image.style.marginLeft, 10) || 0,
      y: parseInt(this.$image.style.marginTop, 10) || 0,
    };
    this.$$on('pointermove', this.onPointerMove);
    this.$$on('pointerup', this.onPointerUp);
    this.setPointerCapture(e.pointerId);
    e.preventDefault();
  }

  protected onPointerUp(e: PointerEvent): void {
    if (!this.inDrag) return;
    e.preventDefault();

    this.inDrag = false;
    this.$$off(this.onPointerMove);
    this.$$off(this.onPointerUp);
    this.releasePointerCapture(e.pointerId);
  }

  protected onPointerMove(e: PointerEvent): void {
    if (!this.inDrag) return;

    const point = getTouchPoint(e);
    const scale = parseInt(this.scale, 10);
    const offsetX = point.x - this.startPoint.x;
    const offsetY = point.y - this.startPoint.y;
    const maxOffsetX = (this.$image.offsetWidth * scale - this.offsetWidth) * 0.5;
    const maxOffsetY = (this.$image.offsetHeight * scale - this.offsetHeight) * 0.5;

    if (maxOffsetX < 0 || maxOffsetY < 0) return;

    const x = this.startPosition.x + offsetX;
    const y = this.startPosition.y + offsetY;

    const normalizedX = Math.min(maxOffsetX, Math.max(-maxOffsetX, x));
    const normalizedY = Math.min(maxOffsetY, Math.max(-maxOffsetY, y));

    this.$image.style.marginTop = `${normalizedY}px`;
    this.$image.style.marginLeft = `${normalizedX}px`;

    e.preventDefault();
  }

  @listen('pointerdown')
  protected onClick(e: PointerEvent): void {
    const isCloseBtn = this.$closeButton.contains(e.target as Node);
    isCloseBtn ? this.unzoom() : this.zoom();
  }

  @listen({event: 'pointerdown', target: 'body'})
  protected onOutsideClick(e: PointerEvent): void {
    if (!this.$image.contains(e.target as Node)) {
      this.unzoom();
    }
  }
}
