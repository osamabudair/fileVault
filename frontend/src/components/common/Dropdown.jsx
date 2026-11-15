import { useState, useRef, useEffect, useCallback } from 'react';
import { createPortal } from 'react-dom';

export const Dropdown = ({
  trigger,
  items = [],
  align = 'right',
  width = 'w-48',
  className = ''
}) => {
  const [isOpen, setIsOpen] = useState(false);
  const [position, setPosition] = useState({
    top: 'auto',
    bottom: 'auto',
    left: '0px',
    openUpward: false
  });
  const triggerRef = useRef(null);
  const menuRef = useRef(null);

  const updatePosition = useCallback(() => {
    if (!triggerRef.current) return;
    const rect = triggerRef.current.getBoundingClientRect();
    
    // Accurate menu height estimation (~32px per item + padding)
    const menuEstimatedHeight = items.length * 32 + 16;
    const menuWidthPx = width === 'w-48' ? 192 : width === 'w-44' ? 176 : 192;

    const spaceBelow = window.innerHeight - rect.bottom;
    const spaceAbove = rect.top;

    // Only flip upward when bottom space is tight and top space is sufficient
    const openUpward = spaceBelow < menuEstimatedHeight + 8 && spaceAbove > spaceBelow;

    let left = align === 'right' ? rect.right - menuWidthPx : rect.left;
    if (left + menuWidthPx > window.innerWidth - 8) {
      left = window.innerWidth - menuWidthPx - 8;
    }
    if (left < 8) {
      left = 8;
    }

    if (openUpward) {
      const bottomOffset = window.innerHeight - rect.top + 4;
      setPosition({
        top: 'auto',
        bottom: `${bottomOffset}px`,
        left: `${left}px`,
        openUpward: true
      });
    } else {
      const topOffset = rect.bottom + 4;
      setPosition({
        top: `${topOffset}px`,
        bottom: 'auto',
        left: `${left}px`,
        openUpward: false
      });
    }
  }, [items.length, align, width]);

  const handleToggle = (e) => {
    e.stopPropagation();
    if (!isOpen) {
      updatePosition();
      setIsOpen(true);
    } else {
      setIsOpen(false);
    }
  };

  useEffect(() => {
    if (!isOpen) return;

    const handleClickOutside = (event) => {
      if (
        triggerRef.current &&
        !triggerRef.current.contains(event.target) &&
        menuRef.current &&
        !menuRef.current.contains(event.target)
      ) {
        setIsOpen(false);
      }
    };

    const handleEscape = (e) => {
      if (e.key === 'Escape') setIsOpen(false);
    };

    // Close on window scroll to ensure ultra-smooth native scroll performance
    const handleScroll = () => {
      setIsOpen(false);
    };

    const handleResize = () => {
      updatePosition();
    };

    document.addEventListener('mousedown', handleClickOutside);
    document.addEventListener('keydown', handleEscape);
    window.addEventListener('resize', handleResize);
    window.addEventListener('scroll', handleScroll, { capture: true, passive: true });

    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
      document.removeEventListener('keydown', handleEscape);
      window.removeEventListener('resize', handleResize);
      window.removeEventListener('scroll', handleScroll, { capture: true });
    };
  }, [isOpen, updatePosition]);

  return (
    <div className={`inline-block ${className}`} ref={triggerRef}>
      <div onClick={handleToggle} className="cursor-pointer">
        {trigger}
      </div>

      {isOpen &&
        createPortal(
          <div
            ref={menuRef}
            style={{
              position: 'fixed',
              top: position.top,
              bottom: position.bottom,
              left: position.left,
              zIndex: 9999,
              maxHeight: 'calc(100vh - 24px)'
            }}
            className={`${width} rounded-xl bg-white/95 dark:bg-[#181B20]/95 border border-black/10 dark:border-white/[0.1] shadow-2xl py-1 text-xs divide-y divide-black/[0.04] dark:divide-white/[0.04] focus:outline-none animate-in fade-in duration-100 ${
              position.openUpward ? 'origin-bottom-right' : 'origin-top-right'
            }`}
            onClick={(e) => e.stopPropagation()}
          >
            <div className="py-0.5">
              {items.map((item, index) => {
                if (item.divider) {
                  return (
                    <div
                      key={`divider-${index}`}
                      className="my-1 border-t border-black/[0.06] dark:border-white/[0.06]"
                    />
                  );
                }

                const Icon = item.icon;
                return (
                  <button
                    key={index}
                    type="button"
                    disabled={item.disabled}
                    onClick={(e) => {
                      e.stopPropagation();
                      if (!item.disabled) {
                        setIsOpen(false);
                        item.onClick && item.onClick();
                      }
                    }}
                    className={`w-full flex items-center gap-2.5 px-3 py-2 text-left transition-colors cursor-pointer disabled:opacity-40 disabled:cursor-not-allowed ${
                      item.danger
                        ? 'text-red-600 dark:text-red-400 hover:bg-red-500/10'
                        : 'text-zinc-700 dark:text-zinc-300 hover:bg-black/[0.05] dark:hover:bg-white/[0.06] hover:text-zinc-900 dark:hover:text-zinc-100'
                    }`}
                  >
                    {Icon && <Icon className="w-3.5 h-3.5 shrink-0 opacity-80" />}
                    <span className="truncate">{item.label}</span>
                  </button>
                );
              })}
            </div>
          </div>,
          document.body
        )}
    </div>
  );
};

export default Dropdown;
