import {
  ESLTabs,
  ESLTab,
  ESLPanel,
  ESLPanelGroup,
  ESLTrigger,
  ESLScrollbar,
  ESLToggleableDispatcher,
  ESLImage,
} from '@exadel/esl';

import {EAKSidebar} from './navigation/navigation';
import {EAKBanner} from './landing/banner/banner';
import {EAKZoomImage} from './eak-zoom-image/eak-zoom-image';

ESLTabs.register();
ESLTab.register();
ESLToggleableDispatcher.init();
ESLPanelGroup.register();
ESLPanel.register();
ESLTrigger.register();
ESLScrollbar.register();
ESLImage.register();

EAKBanner.register();
EAKSidebar.register();
EAKZoomImage.register();
