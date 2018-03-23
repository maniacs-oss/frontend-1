import config from 'lib/config';
import fastdom from 'lib/fastdom-promise';

const formstackPostPath = "/formstack-campaign/submit";

export function init (){
  console.log("something happened");

  // extract data
  const allCampaigns = config.get("page.campaigns");
  console.log("all campaigns", allCampaigns)
  const campaignToShow = allCampaigns.filter(isTip).pop();
  const campaignFields = campaignToShow["fields"];
  const formFields = campaignFields["formFields"];

  // create container, title & description
  const campaignElement = document.createElement("details");
  const summaryElement = document.createElement("summary");
  const titleEl = document.createElement("p");
  const descripEl = document.createElement("p");
  titleEl.textContent = campaignFields["callout"];
  descripEl.textContent = campaignFields["description"];

  // create form
  const formEl = document.createElement("form");
  formEl.setAttribute("method", "post");
  formEl.setAttribute("action", formstackPostPath);
  const submitButton = document.createElement("input");
  submitButton.setAttribute("type", "submit");
  submitButton.setAttribute("value", "Share with the Guardian!!!!");

  // create form fields
  const formFieldEls = formFields.map(field => {
    const fieldset = document.createElement("fieldset");
    const label = document.createElement("label");
    const labelSpan = document.createElement("span");
    const input = document.createElement("input");

    label.textContent = field["label"];
    labelSpan.textContent = field["description"];
    label.setAttribute("for", field["name"]);
    label.appendChild(labelSpan);

    input.setAttribute("type", field["type"]);
    input.setAttribute("name", field["name"]);
    if(field["required"] === "1"){input.setAttribute("required", "true")};

    fieldset.appendChild(label);
    fieldset.appendChild(input);
    return fieldset;
  });

  // append form elements to form
  formFieldEls.forEach(el => {
    formEl.appendChild(el);
  });
  formEl.appendChild(submitButton);

  console.log(formEl);

  // append everything to container
  summaryElement.appendChild(titleEl);
  summaryElement.appendChild(descripEl);
  campaignElement.appendChild(summaryElement);
  campaignElement.appendChild(formEl);

  // select article
  const article = document.querySelector(".content__article-body");
  const endOfArticle = document.querySelector(".content__article-body .submeta");

  // write to DOM
  return fastdom.write(() => {
    return article.insertBefore(campaignElement, endOfArticle);
  });

  function isTip(campaign){
    return campaign.fields._type === "tip";
  }
};
