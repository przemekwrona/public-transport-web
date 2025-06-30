import { ResolveFn } from '@angular/router';

export const usersResolver: ResolveFn<boolean> = (route, state) => {
  return true;
};
