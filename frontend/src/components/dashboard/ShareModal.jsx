import { useState } from 'react';
import { useFiles } from '../../context/FileContext';
import { Modal } from '../common/Modal';
import { Button } from '../common/Button';
import { Copy, Check, Link2, Users } from 'lucide-react';

export const ShareModal = () => {
  const { shareModalFile, setShareModalFile, updateShare } = useFiles();
  const [copied, setCopied] = useState(false);
  const [collaboratorEmail, setCollaboratorEmail] = useState('');

  if (!shareModalFile) return null;

  const shareUrl = `https://filevault.app/share/${shareModalFile.id}`;

  const handleCopy = () => {
    navigator.clipboard?.writeText(shareUrl);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const handleAddCollaborator = (e) => {
    e.preventDefault();
    if (!collaboratorEmail.trim()) return;
    const existing = shareModalFile.sharedWith || [];
    updateShare(shareModalFile.id, [...existing, collaboratorEmail.trim()]);
    setCollaboratorEmail('');
  };

  return (
    <Modal
      isOpen={!!shareModalFile}
      onClose={() => setShareModalFile(null)}
      title="Share File"
      description={`Share "${shareModalFile.name}" with collaborators`}
      maxWidth="max-w-md"
      footer={
        <Button variant="secondary" size="sm" onClick={() => setShareModalFile(null)}>
          Done
        </Button>
      }
    >
      <div className="space-y-4 text-xs">
        {/* Copy Link Input */}
        <div className="space-y-1.5">
          <label className="text-xs font-medium text-zinc-700 dark:text-zinc-300">
            Shareable Link
          </label>
          <div className="flex items-center gap-2">
            <div className="relative flex-1">
              <input
                type="text"
                readOnly
                value={shareUrl}
                className="w-full h-8.5 rounded-xl bg-black/[0.03] dark:bg-white/[0.04] border border-black/10 dark:border-white/[0.08] px-3 text-xs text-zinc-700 dark:text-zinc-300 outline-none select-all"
              />
            </div>
            <Button
              variant={copied ? 'primary' : 'secondary'}
              size="sm"
              icon={copied ? Check : Copy}
              onClick={handleCopy}
              className="shrink-0"
            >
              {copied ? 'Copied' : 'Copy'}
            </Button>
          </div>
        </div>

        {/* Invite by Email */}
        <form onSubmit={handleAddCollaborator} className="space-y-1.5">
          <label className="text-xs font-medium text-zinc-700 dark:text-zinc-300">
            Invite People
          </label>
          <div className="flex items-center gap-2">
            <input
              type="email"
              placeholder="colleague@company.com"
              value={collaboratorEmail}
              onChange={(e) => setCollaboratorEmail(e.target.value)}
              className="flex-1 h-8.5 rounded-xl bg-black/[0.03] dark:bg-white/[0.04] border border-black/10 dark:border-white/[0.08] px-3 text-xs text-zinc-800 dark:text-zinc-200 placeholder:text-zinc-400 outline-none"
            />
            <Button
              type="submit"
              variant="secondary"
              size="sm"
              disabled={!collaboratorEmail.trim()}
            >
              Add
            </Button>
          </div>
        </form>

        {/* Current Collaborators */}
        {shareModalFile.sharedWith && shareModalFile.sharedWith.length > 0 && (
          <div className="space-y-2 pt-1 border-t border-black/[0.05] dark:border-white/[0.06]">
            <span className="text-[11px] font-medium text-zinc-500 flex items-center gap-1.5">
              <Users className="w-3.5 h-3.5" />
              <span>People with access</span>
            </span>
            <div className="space-y-1 max-h-28 overflow-y-auto">
              {shareModalFile.sharedWith.map((person, idx) => (
                <div
                  key={idx}
                  className="flex items-center justify-between py-1 px-2 rounded-lg bg-black/[0.02] dark:bg-white/[0.02] text-xs text-zinc-700 dark:text-zinc-300"
                >
                  <span className="truncate">{person}</span>
                  <span className="text-[10px] text-zinc-400">Can view</span>
                </div>
              ))}
            </div>
          </div>
        )}

        <div className="flex items-center gap-1.5 text-[11px] text-zinc-400">
          <Link2 className="w-3.5 h-3.5" />
          <span>Anyone with the link can view and download</span>
        </div>
      </div>
    </Modal>
  );
};

export default ShareModal;
