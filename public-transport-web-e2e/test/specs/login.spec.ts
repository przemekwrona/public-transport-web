import LoginPage from '../pageobjects/login.page';

describe('Login Page', () => {
    it('should open login page, insert credentials and login to cocpit', async () => {
        LoginPage.open();

        await LoginPage.login('pwrona', 'welcome1');
    });
});
