/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2013 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License along with this program.  If not, see http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org. 
 */

package org.openlmis.functional;


import org.openlmis.UiUtils.CaptureScreenshotOnFailureListener;
import org.openlmis.UiUtils.TestCaseHelper;
import org.openlmis.pageobjects.HomePage;
import org.openlmis.pageobjects.InitiateRnRPage;
import org.openlmis.pageobjects.LoginPage;
import org.openlmis.pageobjects.TemplateConfigPage;
import org.testng.annotations.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import static java.util.Arrays.asList;

@Listeners(CaptureScreenshotOnFailureListener.class)

public class ConfigureProgramTemplate extends TestCaseHelper {

  LoginPage loginPage;

  @BeforeMethod(groups = {"admin"})
  public void setUp() throws InterruptedException, SQLException, IOException {
    super.setup();
    loginPage = PageFactory.getLoginPage(testWebDriver, baseUrlGlobal);
  }

  @Test(groups = {"admin"}, dataProvider = "Data-Provider-Program-Not-Configured")
  public void testVerifyProgramNotConfigured(String program, String userSIC, String password) throws SQLException {
    List<String> rightsList = asList("CREATE_REQUISITION", "VIEW_REQUISITION");
    setupTestDataToInitiateRnR(false, program, userSIC, "200", rightsList);

    HomePage homePage = loginPage.loginAs(userSIC, password);
    homePage.navigateAndInitiateRnr(program);
    InitiateRnRPage initiateRnRPage = homePage.clickProceed();
    initiateRnRPage.verifyTemplateNotConfiguredMessage();

  }

  @Test(groups = {"admin"}, dataProvider = "Data-Provider-Verify-On-Rnr-Screen")
  public void testVerifyImpactOfChangesInConfigScreenOnRnRScreen(String program, String userSIC, String password, String[] credentials) throws SQLException {
    List<String> rightsList = asList("CREATE_REQUISITION", "VIEW_REQUISITION");
    setupTestDataToInitiateRnR(true, program, userSIC, "200", rightsList);

    HomePage homePage = loginPage.loginAs(credentials[0], credentials[1]);
    TemplateConfigPage templateConfigPage = homePage.selectProgramToConfigTemplate(program);
    String newColumnHeading = "Altered";
    templateConfigPage.unClickExpirationDate();
    templateConfigPage.unClickTotal();
    templateConfigPage.checkSkip();
    templateConfigPage.setSkipTextBox("Product Skip");
    templateConfigPage.alterBeginningBalanceLabel(newColumnHeading);

    homePage.logout(baseUrlGlobal);
    HomePage homePageSic = loginPage.loginAs(userSIC, password);
    homePageSic.navigateAndInitiateRnr(program);
    InitiateRnRPage initiateRnRPage = homePage.clickProceed();
    String tableXpathTillTr = "//table[@id='fullSupplyTable']/thead/tr";
    String tableFrozenXpathTillTr = "//table[@id='fullSupplyFrozenTable']/thead/tr";

    int columns = initiateRnRPage.getSizeOfElements(tableXpathTillTr + "/th");
    initiateRnRPage.verifyColumnHeadingNotPresent(tableXpathTillTr, "Expiration Date", columns);
    initiateRnRPage.verifyColumnHeadingNotPresent(tableXpathTillTr, "Total", columns);
    initiateRnRPage.verifyColumnsHeadingPresent(tableFrozenXpathTillTr, "Product Skip\n" + "All | None", columns);
    initiateRnRPage.verifyColumnsHeadingPresent(tableFrozenXpathTillTr, "Product", columns);
    initiateRnRPage.verifyColumnsHeadingPresent(tableFrozenXpathTillTr, "Product Code", columns);
  }

  @Test(groups = {"admin"}, dataProvider = "Data-Provider-Column-Label-Source")
  public void testVerifyColumnLabelsSourceAndMandatoryColumns(String program, String[] credentials) {
    HomePage homePage = loginPage.loginAs(credentials[0], credentials[1]);
    TemplateConfigPage templateConfigPage = homePage.selectProgramToConfigTemplate(program);
    templateConfigPage.verifyMandatoryColumns();
    templateConfigPage.verifyColumnLabels();
    templateConfigPage.verifyColumnSource();
  }

  @Test(groups = {"admin"}, dataProvider = "Data-Provider-Column-Label-Source")
  public void testVerifyArithmeticValidationAndBusinessRules(String program, String[] credentials) {
    HomePage homePage = loginPage.loginAs(credentials[0], credentials[1]);
    TemplateConfigPage templateConfigPage = homePage.selectProgramToConfigTemplate(program);
    templateConfigPage.verifyArithmeticValidations(program);
    templateConfigPage.verifyBusinessRules();
  }

  @AfterMethod(groups = {"admin"})
  public void tearDown() throws SQLException {
    HomePage homePage = PageFactory.getHomePage(testWebDriver);
    homePage.logout(baseUrlGlobal);
    dbWrapper.deleteData();
    dbWrapper.closeConnection();
  }

  @DataProvider(name = "Data-Provider-Column-Label-Source")
  public Object[][] parameterColumnLabelSource() {
    return new Object[][]{
      {"HIV", new String[]{"Admin123", "Admin123"}}
    };
  }

  @DataProvider(name = "Data-Provider-Program-Not-Configured")
  public Object[][] parameterProgramNotConfigured() {
    return new Object[][]{
      {"HIV", "storeInCharge", "Admin123"}
    };
  }

  @DataProvider(name = "Data-Provider-Verify-On-Rnr-Screen")
  public Object[][] parameterVerifyRnRScreen() {
    return new Object[][]{
      {"HIV", "storeInCharge", "Admin123", new String[]{"Admin123", "Admin123"}}
    };
  }
}

