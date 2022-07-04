import {
  ESLTabs,
  ESLTab,
  ESLPanel,
  ESLPanelGroup,
  ESLTrigger,
  ESLScrollbar,
  ESLToggleable,
  ESLToggleableDispatcher,
  ESLImage,
} from '@exadel/esl';

import {EAKSidebar} from './navigation/navigation';
import {EAKBanner} from './landing/banner/banner';
import {EAKAnchorNav} from './eak-anchor-nav/eak-anchor-nav';
import {EAKZoomImage} from './eak-zoom-image/eak-zoom-image';

ESLTabs.register();
ESLTab.register();
ESLToggleableDispatcher.init();
ESLPanelGroup.register();
ESLPanel.register();
ESLToggleable.register();
ESLTrigger.register();
ESLScrollbar.register();
ESLImage.register();

EAKBanner.register();
EAKAnchorNav.register();
EAKSidebar.register();
EAKZoomImage.register();
