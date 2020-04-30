package com.limachi.dimensional_bags.common.data.actions;

import com.limachi.dimensional_bags.common.items.Bag;

public class StandardTriggerFactory {

    private static abstract class BaseStandardTrigger implements IBagTrigger {
        private int id = -1;

        @Override
        public int mappedAction() {
            return this.id;
        }

        @Override
        public void mapAction(int id) {
            this.id = id;
        }
    }

    public static IBagTrigger specific(boolean left, boolean off, boolean crouch) {
        return new BaseStandardTrigger() {
            @Override
            public String printable() {
                return (crouch ? "Crouching " : "Standing ") + (left ? "left click ": "right click ") + (off ? "off " : "main ") + "hand";
            } //longest: 'Crouching right click main hand' (31 chars)

            @Override
            public boolean match(Bag.BagEvent event) {
                return event.offHand == off && event.leftClick == left && event.croushing == crouch;
            }
        };
    }

    /*
    public static IBagTrigger anyLeft() {
        return new BaseStandardTrigger() {
            @Override
            public String printable() {
                return "any left click";
            }

            @Override
            public boolean match(Bag.BagEvent event) {
                return event.leftClick;
            }
        };
    }

    public static IBagTrigger anyOff() {
        return new BaseStandardTrigger() {
            @Override
            public String printable() {
                return "any left off hand (and only right clicks)";
            }

            @Override
            public boolean match(Bag.BagEvent event) {
                return event.offHand;
            }
        };
    }

    public static IBagTrigger standingRight() {
        return new BaseStandardTrigger() {
            @Override
            public String printable() {
                return "any right click without crouching (including off hand)";
            }

            @Override
            public boolean match(Bag.BagEvent event) {
                return !event.leftClick && !event.croushing;
            }
        };
    }

    public static IBagTrigger croushingRight() {
        return new BaseStandardTrigger() {
            @Override
            public String printable() {
                return "any right click while crouching (including off hand)";
            }

            @Override
            public boolean match(Bag.BagEvent event) {
                return !event.leftClick && event.croushing;
            }
        };
    }

    public static IBagTrigger alwaysMatch() {
        return new BaseStandardTrigger() {
            @Override
            public String printable() {
                return "Match any action";
            }

            @Override
            public boolean match(Bag.BagEvent event) {
                return true;
            }
        };
    }
    */
}
