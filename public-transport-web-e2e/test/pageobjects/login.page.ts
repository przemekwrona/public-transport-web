import {ChainablePromiseElement} from 'webdriverio';

import Page from './page';

/**
 * sub page containing specific selectors and methods for a specific page
 */
class LoginPage extends Page {
    /**
     * define selectors using getter methods
     */
    public get inputUsername(): ChainablePromiseElement<WebdriverIO.Element> {
        return $('#email');
    }

    public get inputPassword(): ChainablePromiseElement<WebdriverIO.Element> {
        return $('#password');
    }

    public get btnSubmit(): ChainablePromiseElement<WebdriverIO.Element> {
        return $('button[type="submit"]');
    }

    /**
     * a method to encapsule automation code to interact with the page
     * e.g. to login using username and password
     */
    public async login(username: string, password: string): Promise<void> {
        await this.open();

        await this.inputUsername.setValue(username);
        await this.inputPassword.setValue(password);
        await this.btnSubmit.click();
    }

    /**
     * overwrite specific options to adapt it to page object
     */
    public open(): Promise<string> {
        return super.open('/signin');
    }
}

export default new LoginPage();