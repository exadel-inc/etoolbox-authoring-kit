import {
  ESLTabs,
  ESLTab,
  ESLPanel,
  ESLPanelGroup,
  ESLTrigger,
  ESLScrollbar,
  ESLToggleableDispatcher,
  ESLImage
} from '@exadel/esl';

import { EAKSidebar } from './navigation/navigation';
import { EAKBanner } from './landing/banner/banner';
import { MdImage } from "./MDImage/md-image"

EAKBanner.register();
EAKSidebar.register();

ESLTabs.register();
ESLTab.register();
ESLToggleableDispatcher.init();
ESLPanelGroup.register();
ESLPanel.register();
ESLTrigger.register();
ESLScrollbar.register();
ESLImage.register();
MdImage.register();