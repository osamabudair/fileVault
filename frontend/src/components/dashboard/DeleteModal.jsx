import { useFiles } from '../../context/FileContext';
import { Modal } from '../common/Modal';
import { Button } from '../common/Button';
import { Trash2 } from 'lucide-react';
import { formatBytes } from '../../api/mockData';

export const DeleteModal = () => {
  const { deleteModalFile, setDeleteModalFile, deleteFile, activeTab } = useFiles();

  if (!deleteModalFile) return null;

  const isPermanent = deleteModalFile.isTrash || activeTab === 'trash';

  const handleDelete = () => {
    deleteFile(deleteModalFile.id, isPermanent);
  };

  return (
    <Modal
      isOpen={!!deleteModalFile}
      onClose={() => setDeleteModalFile(null)}
      title={isPermanent ? 'Delete Permanently' : 'Move to Trash'}
      description={
        isPermanent
          ? 'This will permanently delete the file from your vault. This action cannot be undone.'
          : 'Are you sure you want to move this file to trash?'
      }
      maxWidth="max-w-sm"
      footer={
        <>
          <Button
            variant="secondary"
            size="sm"
            onClick={() => setDeleteModalFile(null)}
          >
            Cancel
          </Button>
          <Button
            variant="danger"
            size="sm"
            icon={Trash2}
            onClick={handleDelete}
          >
            {isPermanent ? 'Delete' : 'Move to Trash'}
          </Button>
        </>
      }
    >
      <div className="p-3 rounded-xl bg-black/[0.02] dark:bg-white/[0.02] border border-black/[0.04] dark:border-white/[0.04] text-xs space-y-1">
        <p className="font-medium text-zinc-900 dark:text-zinc-100 truncate">
          {deleteModalFile.name}
        </p>
        <p className="text-[11px] text-zinc-400">
          {formatBytes(deleteModalFile.size)} • {deleteModalFile.category}
        </p>
      </div>
    </Modal>
  );
};

export default DeleteModal;
