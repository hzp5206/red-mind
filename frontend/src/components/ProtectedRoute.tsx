import { Spin } from 'antd';
import { useEffect, useState } from 'react';
import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { getCurrentUser } from '../api/user';
import { isLoggedIn, setAuthProfile } from '../utils/auth';

export function ProtectedRoute() {
  const location = useLocation();
  const [loading, setLoading] = useState(true);
  const [allowed, setAllowed] = useState(false);

  useEffect(() => {
    if (!isLoggedIn()) {
      setAllowed(false);
      setLoading(false);
      return;
    }

    getCurrentUser()
      .then(({ data }) => {
        setAuthProfile({
          nickname: data.data.nickname,
          role: data.data.role,
          roleCode: data.data.roleCode,
          permissions: data.data.permissions,
        });
        const nextAllowed = !location.pathname.startsWith('/admin') || data.data.role === 'ADMIN';
        setAllowed(nextAllowed);
      })
      .catch(() => {
        setAllowed(false);
      })
      .finally(() => {
        setLoading(false);
      });
  }, [location.pathname]);

  if (!isLoggedIn()) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }

  if (loading) {
    return <div style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center' }}><Spin size="large" /></div>;
  }

  if (location.pathname.startsWith('/admin') && !allowed) {
    return <Navigate to="/create" replace />;
  }

  return <Outlet />;
}
