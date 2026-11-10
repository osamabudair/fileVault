/* eslint-disable react-refresh/only-export-components */
import { createContext, useContext, useState, useMemo, useCallback } from 'react';
import { INITIAL_FILES, getFileCategory } from '../api/mockData';

const FileContext = createContext(null);

export const FileProvider = ({ children }) => {
  const [files, setFiles] = useState(INITIAL_FILES);
  const [activeTab, setActiveTab] = useState('all'); // 'all' | 'shared' | 'favorites' | 'trash'
  const [selectedCategory, setSelectedCategory] = useState('all'); // 'all' | 'documents' | 'media' | 'archives' | 'code'
  const [searchQuery, setSearchQuery] = useState('');
  const [viewMode, setViewMode] = useState('table'); // 'table' | 'grid'
  
  // Modals
  const [shareModalFile, setShareModalFile] = useState(null);
  const [deleteModalFile, setDeleteModalFile] = useState(null);

  // Toast
  const [toast, setToast] = useState(null);

  const showToast = useCallback((message, type = 'info') => {
    setToast({ id: Date.now(), message, type });
    setTimeout(() => {
      setToast(prev => (prev?.message === message ? null : prev));
    }, 2800);
  }, []);

  const addFiles = useCallback((fileList) => {
    if (!fileList || fileList.length === 0) return;
    const added = Array.from(fileList).map(f => ({
      id: `fl_${Date.now().toString(36)}_${Math.random().toString(36).substring(2, 6)}`,
      name: f.name,
      size: f.size,
      type: f.type || 'application/octet-stream',
      category: getFileCategory(f.name, f.type),
      updatedAt: new Date().toISOString(),
      isShared: false,
      downloadUrl: '#',
      isFavorite: false,
      isTrash: false,
      sharedWith: []
    }));

    setFiles(prev => [...added, ...prev]);
    showToast(`Uploaded ${added.length} file${added.length > 1 ? 's' : ''}`, 'success');
  }, [showToast]);

  const toggleFavorite = useCallback((id) => {
    setFiles(prev =>
      prev.map(f => {
        if (f.id === id) {
          const next = !f.isFavorite;
          showToast(next ? `Added to favorites` : `Removed from favorites`, 'info');
          return { ...f, isFavorite: next };
        }
        return f;
      })
    );
  }, [showToast]);

  const deleteFile = useCallback((id, permanent = false) => {
    setFiles(prev => {
      const target = prev.find(f => f.id === id);
      if (!target) return prev;
      if (permanent || target.isTrash) {
        showToast(`Permanently deleted ${target.name}`, 'warning');
        return prev.filter(f => f.id !== id);
      } else {
        showToast(`Moved ${target.name} to Trash`, 'info');
        return prev.map(f => (f.id === id ? { ...f, isTrash: true } : f));
      }
    });
    setDeleteModalFile(null);
  }, [showToast]);

  const restoreFile = useCallback((id) => {
    setFiles(prev =>
      prev.map(f => {
        if (f.id === id) {
          showToast(`Restored ${f.name}`, 'success');
          return { ...f, isTrash: false };
        }
        return f;
      })
    );
  }, [showToast]);

  const updateShare = useCallback((id, sharedWith) => {
    setFiles(prev =>
      prev.map(f => (f.id === id ? { ...f, isShared: true, sharedWith } : f))
    );
    showToast('Share link updated', 'success');
  }, [showToast]);

  const downloadFile = useCallback((file) => {
    showToast(`Downloading ${file.name}...`, 'info');
  }, [showToast]);

  const filteredFiles = useMemo(() => {
    return files.filter(f => {
      // Tab filter
      if (activeTab === 'trash') {
        if (!f.isTrash) return false;
      } else {
        if (f.isTrash) return false;
        if (activeTab === 'shared' && !f.isShared) return false;
        if (activeTab === 'favorites' && !f.isFavorite) return false;
      }

      // Category filter
      if (selectedCategory !== 'all') {
        if (f.category !== selectedCategory) return false;
      }

      // Search
      if (searchQuery.trim()) {
        const q = searchQuery.toLowerCase().trim();
        if (!f.name.toLowerCase().includes(q) && !f.category.toLowerCase().includes(q)) {
          return false;
        }
      }

      return true;
    });
  }, [files, activeTab, selectedCategory, searchQuery]);

  const counts = useMemo(() => ({
    all: files.filter(f => !f.isTrash).length,
    shared: files.filter(f => !f.isTrash && f.isShared).length,
    favorites: files.filter(f => !f.isTrash && f.isFavorite).length,
    trash: files.filter(f => f.isTrash).length
  }), [files]);

  return (
    <FileContext.Provider
      value={{
        files,
        filteredFiles,
        counts,
        activeTab,
        setActiveTab,
        selectedCategory,
        setSelectedCategory,
        searchQuery,
        setSearchQuery,
        viewMode,
        setViewMode,
        addFiles,
        deleteFile,
        restoreFile,
        toggleFavorite,
        updateShare,
        downloadFile,
        shareModalFile,
        setShareModalFile,
        deleteModalFile,
        setDeleteModalFile,
        toast,
        showToast
      }}
    >
      {children}
    </FileContext.Provider>
  );
};

export const useFiles = () => {
  const context = useContext(FileContext);
  if (!context) {
    throw new Error('useFiles must be used within a FileProvider');
  }
  return context;
};

export default FileContext;
