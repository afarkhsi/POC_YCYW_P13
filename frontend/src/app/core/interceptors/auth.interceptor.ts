import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const stored = localStorage.getItem('user');

  if (stored) {
    try {
      const user = JSON.parse(stored);
      if (user?.token) {
        const cloned = req.clone({
          setHeaders: {
            Authorization: `Bearer ${user.token}`,
          },
        });
        return next(cloned);
      }
    } catch {
      localStorage.removeItem('user');
    }
  }

  return next(req);
};
