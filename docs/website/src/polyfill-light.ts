// IE, Safari < 13.3
import {ResizeObserver} from '@juggle/resize-observer';
window.ResizeObserver = window.ResizeObserver || ResizeObserver;

// Safari < 14
import SmoothScroll from 'smoothscroll-polyfill';
SmoothScroll.polyfill();
